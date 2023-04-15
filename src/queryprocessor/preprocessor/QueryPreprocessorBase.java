package queryprocessor.preprocessor;

import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import queryprocessor.preprocessor.exceptions.InvalidQueryException;
import queryprocessor.preprocessor.exceptions.MissingArgumentException;
import queryprocessor.preprocessor.validators.ValidatorFactory;
import queryprocessor.querytree.*;
import utils.Pair;

public class QueryPreprocessorBase implements QueryPreprocessor
{
    private final HashSet<Synonym<?>> declaredSynonyms = new HashSet<>();
    private final static HashSet<Keyword> synonymsKeywords = new HashSet<>(
            Arrays.asList(
                    Keyword.WHILE,
                    Keyword.ASSIGN,
                    Keyword.STATEMENT,
                    Keyword.IF,
                    Keyword.PROCEDURE,
                    Keyword.CONSTANT,
                    Keyword.VARIABLE)
    );
    private final static HashSet<Keyword> relationshipsKeywords = new HashSet<>(
            Arrays.asList(
                    Keyword.FOLLOWS, Keyword.T_FOLLOWS,
                    Keyword.PARENT, Keyword.T_PARENT,
                    Keyword.MODIFIES, Keyword.T_MODIFIES,
                    Keyword.CALLS, Keyword.T_CALLS,
                    Keyword.USES, Keyword.T_USES,
                    Keyword.AFFECTS, Keyword.T_AFFECTS)
    );

    private final static String querySeparator = ";";

    private class ResultSynonymExtractor {
        public List<ResNode> extractSynonyms(String query) throws InvalidQueryException {
            var resultNodes = new ArrayList<ResNode>();

            var matcher = Pattern.compile(Keyword.SYNONYMS.getRegExpr(), Pattern.CASE_INSENSITIVE).matcher(query);

            if(!matcher.find())
                return Collections.emptyList();

            var group = matcher.group().trim();

            List<String> identifiers = new ArrayList<>();
            if(group.contains(",")) {
                if(!group.matches(Keyword.RESULT_TUPLE.getRegExpr()))
                    throw new InvalidQueryException("Invalid result tuple format. Expected e.g. <res1, res2, ...>", group);

                var resTuple = group.replaceAll("[<>]", "");
                identifiers = Arrays.stream(resTuple.split(",")).map(String::trim).collect(Collectors.toList());
            }
            else {
                if(group.isBlank())
                    return Collections.emptyList();

                var booleanMatcher = Pattern.compile(Keyword.BOOLEAN.getRegExpr(), Pattern.CASE_INSENSITIVE).matcher(group);

                if(booleanMatcher.find())
                    resultNodes.add(new ResBooleanNode());
                else
                    identifiers.add(group.trim());
            }

            for (var id: identifiers) {
                var synonym = findSynonym(id);
                if(synonym == null)
                    throw new InvalidQueryException(String.format("Undeclared synonym %s in the select clause", id),  group);

                resultNodes.add(new ResNode(synonym));
            }

            return resultNodes;
        }
    }

    private class RelationshipExtractor
    {
        public List<RelationshipRef> extractRelationships(String query) throws InvalidQueryException, MissingArgumentException
        {
            var relationships = new ArrayList<RelationshipRef>();

            var start = query.length();
            var end = query.length();
            for (Keyword rel: relationshipsKeywords) {
                var relMatcher = Pattern.compile(rel.getRegExpr(), Pattern.CASE_INSENSITIVE).matcher(query);
                var matchResults = relMatcher.results().collect(Collectors.toList());

                if(matchResults.isEmpty())
                    continue;

                for (var match: matchResults)
                {
                    if(match.start() < start)
                        start = match.start();

                    if(match.end() > end)
                        end = match.end();

                    var args = extractArguments(query, rel, match.start(), match.end());
                    relationships.add(new RelationshipRef(rel, args));
                }
            }

            if(!relationships.isEmpty() && (relationships.size()-1) != getConcatenatorCount(query, start, end))
                throw new InvalidQueryException("Missing 'and' concatenator in between relationships", query);

            return relationships;
        }

        public List<Synonym<?>> extractArguments(String query, Keyword relType, int start, int end) throws MissingArgumentException, InvalidQueryException
        {
            var argsMatcher = Pattern.compile(Keyword.ARGS.getRegExpr(), Pattern.CASE_INSENSITIVE).matcher(query).region(start, end);

            if(!argsMatcher.find())
                throw new MissingArgumentException(relType.getName(), 0, query);

            var args = argsMatcher.group().split(",");

            var arguments = new ArrayList<Synonym<?>>();
            for (var arg: args) {
                var synonym = findSynonym(arg.trim());

                if(synonym == null)
                    throw new InvalidQueryException("Unrecognized parameter synonym", arg);

                arguments.add(synonym);
            }

           return arguments;
        }

        public long getConcatenatorCount(String line, int start, int end)
        {
            var matcher = Pattern.compile(Keyword.AND.getRegExpr(), Pattern.CASE_INSENSITIVE).matcher(line).region(start, end);
            return matcher.results().count();
        }
    }

    private class ConditionExtractor
    {
        public List<ConditionNode> extractConditions(String query) throws InvalidQueryException
        {
            var conditions = new ArrayList<ConditionNode>();

            var matcher = Pattern.compile(Keyword.WITH_CLAUSE.getRegExpr(), Pattern.CASE_INSENSITIVE).matcher(query);
            var matchResults = matcher.results().collect(Collectors.toList());

            for (var match: matchResults) {
                var result = match.group();
                var pair = extractAttributes(result);
                conditions.add(new ConditionNode(pair.getFirst(), pair.getSecond()));
            }

            return conditions;
        }

        public Pair<AttrRef, AttrValue> extractAttributes(String group) throws InvalidQueryException {
            var cond = Arrays.stream(group.split("=")).map(String::trim).collect(Collectors.toList());
            cond.set(0, cond.get(0).replaceAll(Keyword.WITH.getRegExpr(), "").trim());

            var res = Arrays.stream(cond.get(0).split("\\.")).map(String::trim).collect(Collectors.toList());

            var synonym = findSynonym(res.get(0));
            var attr = findAttrName(res.get(1));

            if(synonym == null)
                throw new InvalidQueryException(String.format("Unrecognized synonym %s", res.get(0)), group);

            if(attr == null)
                throw new InvalidQueryException(String.format("Unrecognized attribute %s", res.get(1)), group);

            var value = cond.get(1);

            if(value.contains("\""))
                value = value.replaceAll("\"", "");

            return new Pair<>(new AttrRef(synonym, attr), new AttrValue(value));
        }
    }

    public QueryTree parseQuery(String query) throws InvalidQueryException, MissingArgumentException
    {
        if (query.length() == 0)
            throw new InvalidQueryException("Empty query");

        var array = query.split(querySeparator); // positive look behind - query.split("((?<=;))");

        return parseQuery(array);
    }

    public QueryTree parseQuery(String[] queryLines) throws InvalidQueryException, MissingArgumentException
    {
        declaredSynonyms.clear();

        if (queryLines.length == 0)
            throw new InvalidQueryException("The query not properly ended. "+querySeparator+" is missing", 1);

        var tree = new QTree();

        boolean selectClauseExists = false;
        for (int i = 0; i < queryLines.length; i++)
        {
            var line = queryLines[i];
            if (line.length() == 0)
                continue;

            // Check if the current line is a synonym declaration or the query itself (contains a select clause)
            if (isQuery(line)) {
                if (selectClauseExists)
                    throw new InvalidQueryException("Multiple select clauses", i, line);
                else
                    selectClauseExists = true;
            }

            // Synonyms declarations
            if (!selectClauseExists)
                parseSynonymDeclarations(line, i);
            else
            {
                //region SELECT CLAUSE
                var rse = new ResultSynonymExtractor();
                var resultSynonyms = rse.extractSynonyms(line);

                if(resultSynonyms.isEmpty())
                    throw new InvalidQueryException("Empty select results. You need to specify the result or result's tuple in the select clause", i, line);

                tree.createResultsNode();

                for (var res: resultSynonyms) {
                    tree.addResNode(res);
                }

                //endregion

                // region SUCH THAT
                if(containsSuchThatClause(line))
                {
                    var re = new RelationshipExtractor();
                    var relationships = re.extractRelationships(line);

                    tree.createSuchThatNode();

                    if(relationships.isEmpty())
                        throw new InvalidQueryException("Invalid 'such that' clause", i, line);

                    for (var rel: relationships) {
                        var validator = ValidatorFactory.createRelationshipValidator(rel);

                        if(!validator.isValid())
                            throw new InvalidQueryException(validator.getErrorMsg(), i, rel.getLabel());

                        tree.addRelationshipNode(rel);
                    }

                    tree.getResultsNode().setRightSibling(tree.getSuchThatNode()); // optional, only to make traversing easy
                }
                //endregion

                //region WITH
                if(containsWithClause(line))
                {
                    var ce = new ConditionExtractor();
                    var conditions = ce.extractConditions(line);

                    tree.createWithNode();

                    if(conditions.isEmpty())
                        throw new InvalidQueryException("Invalid 'with' clause", i, line);

                    for (var cond: conditions) {
                        var validator = ValidatorFactory.createConditionValidator(cond);

                        if(!validator.isValid())
                            throw new InvalidQueryException(validator.getErrorMsg(), i, line);

                        tree.addConditionNode(cond);
                    }

                    if(tree.getSuchThatNode() != null) // Opcjonalne, tylko po to zeby traversowac drzewko do printowania
                        tree.getSuchThatNode().setRightSibling(tree.getWithNode());
                    else
                        tree.getResultsNode().setRightSibling(tree.getWithNode());
                }
                //endregion
            }
        }

        if (!selectClauseExists) {
            throw new InvalidQueryException("No select clause was found");
        }

        return tree;
    }

    private void parseSynonymDeclarations(String line, int lineNumber) throws InvalidQueryException
    {
        Keyword type = null;
        int start = 0;
        int cnt = 0;

        for (Keyword keyword : synonymsKeywords) {
            var matcher = Pattern.compile(Pattern.quote(keyword.getRegExpr()),
                    Pattern.CASE_INSENSITIVE).matcher(line);

            if (matcher.find()) {
                type = keyword;
                start = matcher.end();
                cnt++;
            }
        }

        if (cnt > 1)
            throw new InvalidQueryException("Multiple synonym types in one declaration", lineNumber, line);

        if (type == null)
            throw new InvalidQueryException("Unrecognized synonym definition", lineNumber, line);

        var synonyms = Pattern.compile(Keyword.SYNONYM.getRegExpr())
                .matcher(line)
                .region(start, line.length())
                .results()
                .map(MatchResult::group)
                .collect(Collectors.toList());

        if (synonyms.isEmpty())
            throw new InvalidQueryException("Missing synonym identifier", lineNumber, line);

        for (String synonym : synonyms) {
            var s = SynonymFactory.create(synonym, type);
            if (declaredSynonyms.contains(s)) {
                throw new InvalidQueryException("Synonym already declared", lineNumber, synonym);
            } else {
                declaredSynonyms.add(s);
            }
        }
    }

    private boolean containsWithClause(String line) {
        var matcher = Pattern.compile(Pattern.quote(Keyword.WITH.getRegExpr()), Pattern.CASE_INSENSITIVE).matcher(line);
        return matcher.find();
    }

    private boolean containsSuchThatClause(String line) {
        var pattern = Pattern.compile(Pattern.quote(Keyword.SUCH_THAT.getRegExpr()), Pattern.CASE_INSENSITIVE);
        return pattern.matcher(line).find();
    }

    private boolean isQuery(String line) {
        var pattern = Pattern.compile(Keyword.SELECT.getRegExpr(), Pattern.CASE_INSENSITIVE);
        return pattern.matcher(line).find();
    }

    @SuppressWarnings("unused")
    private Keyword findRelationship(String relationship) {
        for (var rel: relationshipsKeywords)
        {
            var relMatcher = Pattern.compile(Pattern.quote(rel.getRegExpr()), Pattern.CASE_INSENSITIVE).matcher(relationship);
            if(relMatcher.find())
                return rel;
        }

        return null;
    }

    private Synonym<?> findSynonym(String id) {
        var f = this.declaredSynonyms.stream().filter(s -> s.getIdentifier().equals(id)).findFirst();

        return f.orElse(null);
    }

    private AttrName findAttrName(String name) {
        for (var attr: AttrName.values()) {
            if(name.equals(attr.getName()))
                return attr;
        }

        return null;
    }
}
