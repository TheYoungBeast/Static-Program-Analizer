package queryprocessor.preprocessor;

import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import queryprocessor.preprocessor.exceptions.InvalidQueryException;
import queryprocessor.preprocessor.exceptions.MissingArgumentException;
import queryprocessor.preprocessor.synonyms.StatementIdSynonym;
import queryprocessor.preprocessor.synonyms.Synonym;
import queryprocessor.preprocessor.synonyms.SynonymFactory;
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
        private final ParsingProgress parsingProgress;

        public ResultSynonymExtractor(ParsingProgress parsingProgress) {
            this.parsingProgress = parsingProgress;
        }

        public List<ResNode> extractSynonyms(String query) throws InvalidQueryException {
            var resultNodes = new ArrayList<ResNode>();

            var matcher = Pattern.compile(Keyword.SYNONYMS.getRegExpr(), Pattern.CASE_INSENSITIVE).matcher(query);

            if(!matcher.find())
                return Collections.emptyList();

            var group = matcher.group().trim();

            parsingProgress.setParsed(matcher.toMatchResult().start(), matcher.toMatchResult().end());

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
        private final ParsingProgress parsingProgress;

        public RelationshipExtractor(ParsingProgress parsingProgress) {
            this.parsingProgress = parsingProgress;
        }

        public List<RelationshipRef> extractRelationships(String query) throws InvalidQueryException, MissingArgumentException
        {
            var relationships = new ArrayList<RelationshipRef>();

            var start = query.length();
            var end = 0;
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
                    parsingProgress.setParsed(match.start(), match.end());
                }
            }

            if(!relationships.isEmpty() && (relationships.size()-1) != getConcatenatorCount(query, start, end))
                throw new InvalidQueryException("Missing 'and' concatenator in between relationships", query);

            return relationships;
        }

        public List<Synonym<?>> extractArguments(String query, Keyword relType, int start, int end) throws MissingArgumentException, InvalidQueryException
        {
            var argsMatcher = Pattern.compile(Keyword.REL_ARGS.getRegExpr(), Pattern.CASE_INSENSITIVE).matcher(query).region(start, end);

            var test = query.substring(start, end);
            if(!argsMatcher.find())
                throw new MissingArgumentException(relType.getName(), 0, query);

            var args = argsMatcher.group().split(",");

            var arguments = new ArrayList<Synonym<?>>();
            var argN = 0;
            for (var arg: args) {
                argN++;
                arg = arg.trim();

                Synonym<?> synonym;
                if(arg.contains("\"")) {
                    var td = new ArgumentTypeDeducer();
                    arg = arg.replaceAll("\"", "").trim();
                    synonym = td.deduce(relType, arg, argN);
                }
                else if(isNumeric(arg))
                    synonym = new StatementIdSynonym(arg);
                else
                    synonym = findSynonym(arg.trim());

                if(synonym == null)
                    throw new InvalidQueryException("Unrecognized parameter synonym", arg);

                arguments.add(synonym);
            }

           return arguments;
        }

        public long getConcatenatorCount(String line, int start, int end)
        {
            var matcher = Pattern.compile(Keyword.AND.getRegExpr(), Pattern.CASE_INSENSITIVE).matcher(line).region(start, end);
            var results = matcher.results().collect(Collectors.toList());

            if(!results.isEmpty())
                results.forEach(matchResult -> parsingProgress.setParsed(matchResult.start(), matchResult.end()));

            return results.size();
        }

        private boolean isNumeric(String strNum) {
            final Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
            if (strNum == null)
                return false;

            return pattern.matcher(strNum).matches();
        }
    }

    private class ConditionExtractor
    {
        private final ParsingProgress parsingProgress;

        public ConditionExtractor(ParsingProgress parsingProgress) {
            this.parsingProgress = parsingProgress;
        }

        public List<ConditionNode> extractConditions(String query) throws InvalidQueryException
        {
            var conditions = new ArrayList<ConditionNode>();

            var matcher = Pattern.compile(Keyword.WITH_CLAUSE.getRegExpr(), Pattern.CASE_INSENSITIVE).matcher(query);
            var matchResults = matcher.results().collect(Collectors.toList());

            for (var match: matchResults) {
                var result = match.group();
                conditions.addAll(extractAttributes(result));
                parsingProgress.setParsed(match.start(), match.end());
            }

            return conditions;
        }

        public List<ConditionNode> extractAttributes(String group) throws InvalidQueryException {
            var conditionPart = Arrays.stream(group.split("=")).map(String::trim).collect(Collectors.toList());
            conditionPart.set(0, conditionPart.get(0).replaceAll(Keyword.WITH.getRegExpr(), "").trim());

            List<ConditionNode> conditionNodes = new ArrayList<>();
            List<Pair<Synonym<?>, AttrName>> refs = new ArrayList<>();

            for (var c: conditionPart) {
                if(!c.contains("."))
                    continue;

                var attrs = Arrays.stream(c.split("\\.")).map(String::trim).collect(Collectors.toList());

                var synonym = findSynonym(attrs.get(0));
                var attr = findAttrName(attrs.get(1));

                if(synonym == null)
                    throw new InvalidQueryException(String.format("Unrecognized synonym %s", attrs.get(0)), group);

                if(attr == null)
                    throw new InvalidQueryException(String.format("Unrecognized attribute %s", attrs.get(1)), group);

                refs.add(new Pair<>(synonym, attr));
            }

            if(refs.size() == 1) {
                var value = conditionPart.get(1);

                if (value.contains("\""))
                    value = value.replaceAll("\"", "");

                var pair = refs.get(0);
                var attrRef = new AttrRef(pair.getFirst(), pair.getSecond());
                var attrVal = new AttrValue(value);
                conditionNodes.add(new ConditionRefValue(attrRef, attrVal));
            }
            else {
                var ref1 = new AttrRef(refs.get(0).getFirst(), refs.get(0).getSecond());
                var ref2 = new AttrRef(refs.get(1).getFirst(), refs.get(1).getSecond());
                conditionNodes.add(new ConditionRefRef(ref1, ref2));
            }

            return conditionNodes;
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
        if (queryLines.length == 0)
            throw new InvalidQueryException("The query not properly ended. "+querySeparator+" is missing", 1);

        var progress = new ParsingProgress(queryLines[queryLines.length-1]);
        var tree = new QTree();
        declaredSynonyms.clear();

        boolean selectClauseExists = false;
        for (int i = 0; i < queryLines.length; i++)
        {
            var line = queryLines[i];
            if (line.length() == 0)
                continue;

            // Check if the current line is a synonym declaration or the query itself (contains a select clause)
            if (containsClause(Keyword.SELECT, line, progress)) {
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
                var rse = new ResultSynonymExtractor(progress);
                var resultSynonyms = rse.extractSynonyms(line);

                if(resultSynonyms.isEmpty())
                    throw new InvalidQueryException("Empty select results. You need to specify the result or result's tuple in the select clause", i, line);

                tree.createResultsNode();

                for (var res: resultSynonyms) {
                    tree.addResNode(res);
                }

                //endregion

                // region SUCH THAT
                if(containsClause(Keyword.SUCH_THAT, line, progress))
                {
                    var re = new RelationshipExtractor(progress);
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
                if(containsClause(Keyword.WITH, line, progress))
                {
                    var ce = new ConditionExtractor(progress);
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

        if(!progress.isCompleted())
            throw new InvalidQueryException("Failed to parse the query. Please check the syntax.", queryLines[queryLines.length-1]);

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

    private boolean containsClause(Keyword clause, String query, ParsingProgress parsingProgress) {
        var matcher = Pattern.compile(clause.getRegExpr(), Pattern.CASE_INSENSITIVE).matcher(query);
        var res = matcher.find();
        if(res)
            parsingProgress.setParsed(matcher.toMatchResult().start(), matcher.toMatchResult().end());

        return res;
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
