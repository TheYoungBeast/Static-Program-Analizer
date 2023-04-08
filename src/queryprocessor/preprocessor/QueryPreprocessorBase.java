package queryprocessor.preprocessor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import queryprocessor.preprocessor.exceptions.InvalidQueryException;
import queryprocessor.preprocessor.exceptions.MissingArgumentException;
import queryprocessor.preprocessor.validators.CallsValidator;
import queryprocessor.querytree.*;

public class QueryPreprocessorBase implements QueryPreprocessor
{
    private final HashSet<Synonym<?>> declaredSynonyms = new HashSet<>();
    private final static HashSet<Keyword> synonymsKeywords = new HashSet<>(
            Arrays.asList(
                    Keyword.WHILE,
                    Keyword.ASSIGN,
                    Keyword.STATEMENT,
                    Keyword.PROCEDURE,
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

    private final static String selectRegex = "select(\\s*)[a-zA-Z]+[0-9]*(\\s*)(,*(\\s*)[a-zA-Z]*[0-9]*)*";
    private final static String querySeparator = ";";

    private class RelationshipExtractor
    {
        public List<RelationshipRef> extractRelationships(String line) throws InvalidQueryException, MissingArgumentException
        {
            var relationships = new ArrayList<RelationshipRef>();

            MatchResult lastResult = null;
            for (Keyword rel: relationshipsKeywords) {
                var relMatcher = Pattern.compile(rel.getRegExpr(), Pattern.CASE_INSENSITIVE).matcher(line);
                var matchResults = relMatcher.results().collect(Collectors.toList());

                if(matchResults.isEmpty())
                    continue;

                for (var match: matchResults)
                {
                    if(lastResult != null)
                        // contains at least 1 relations ships
                        // this means that Concatenator has to be present between relationships
                    {
                        var start = lastResult.end();
                        var end = match.start();

                        if(start > end) {
                            var t = end;
                            end = start;
                            start = t;
                        }

                        if(!containsConcatenator(line, start, end)) // e.g. [...] Parent(s1, s2) AND Uses(s1, "x")
                            throw new InvalidQueryException("Missing concatenator in between relationships", line);
                    }

                    var args = extractArguments(line, rel, match.start(), match.end());
                    relationships.add(new RelationshipRef(rel, args));

                    lastResult = match;
                }
            }

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

        public boolean containsConcatenator(String line, int start, int end)
        {
            var matcher = Pattern.compile(Keyword.AND.getRegExpr(), Pattern.CASE_INSENSITIVE).matcher(line).region(start, end);
            return matcher.find();
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
            // Query
            {
                //region SELECT CLAUSE
                var matcher = Pattern.compile(Keyword.SELECT.getRegExpr(), Pattern.CASE_INSENSITIVE)
                        .matcher(line);

                var start = 0;
                if (matcher.find())
                    start = matcher.end();
                else
                    throw new UnknownError();

                // get all synonyms in select clause
                var selectVars = Pattern.compile(Keyword.SYNONYM.getRegExpr())
                        .matcher(line).region(start, line.length())
                        .results()
                        .map(mr -> mr.group().trim())
                        .collect(Collectors.toList());

                for (String v : selectVars)
                {
                    if(this.findSynonym(v) == null)
                        throw new InvalidQueryException("Undeclared synonym " + v + " in the select clause", i);
                }

                tree.createResultsNode();

                for (String v : selectVars) {
                    var resNode = new ResNode( this.findSynonym(v) );
                    tree.addResNode(resNode);
                }
                //endregion

                // region SUCH THAT
                if(containsSuchThatClause(line))
                {
                    var re = new RelationshipExtractor();
                    var relationships = re.extractRelationships(line);

                    tree.createSuchThatNode();

                    for (var rel: relationships) {
                        if(rel.getRelationshipType() == Keyword.CALLS)
                        {
                            var v = new CallsValidator(rel);
                            if(!v.isValid())
                                throw new InvalidQueryException(v.getErrorMsg(), i, rel.getLabel());
                        }
                        tree.addRelationshipNode(rel);
                    }

                    tree.getResultsNode().setRightSibling(tree.getSuchThatNode()); // optional, only to make traversing easy
                }
                //endregion

                //region WITH
                matcher = Pattern.compile(Pattern.quote(Keyword.WITH.getRegExpr()), Pattern.CASE_INSENSITIVE).matcher(line);

                // with clause
                if(matcher.find()) {
                    var wStart = matcher.end();

                    var attrs = Pattern.compile(Keyword.ATTR_COND.getRegExpr(), Pattern.CASE_INSENSITIVE)
                            .matcher(line).region(wStart, line.length())
                            .results()
                            .map(mr -> mr.group().trim())
                            .collect(Collectors.toList());

                    if(attrs.isEmpty())
                        throw new InvalidQueryException("Invalid 'with clause'", i, line);


                    var condMatcher = Pattern.compile(Keyword.ATTR2.getRegExpr(), Pattern.CASE_INSENSITIVE).matcher(line).region(wStart, line.length());

                    var attrValues = condMatcher
                            .results()
                            .map(mr -> mr.group().trim())
                            .collect(Collectors.toList());

                    if(attrs.size() != attrValues.size())
                        throw new InvalidQueryException("Missing attribute value in 'with clause'", i, line.substring(wStart));

                    // loop
                    var res = attrs.get(0).split("\\.");
                    var synonym = this.findSynonym(res[0].trim());
                    var attr = this.findAttrName(res[1].trim());

                    if(attr == null)
                        throw new InvalidQueryException("Unrecognized attribute", i, res[1].trim());

                    var cond = new ConditionNode(new AttrRef(synonym, attr), attrValues.get(0));

                    var wthNode = new WithNode();
                    tree.setWithNode(wthNode);
                    cond.setParent(cond);
                    wthNode.setFirstChild(cond);

                    if(tree.getSuchThatNode() != null)
                        tree.getSuchThatNode().setRightSibling(wthNode);
                    else
                        tree.getResultsNode().setRightSibling(wthNode);
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

    private boolean containsSuchThatClause(String line) {
        var pattern = Pattern.compile(Pattern.quote(Keyword.SUCH_THAT.getRegExpr()), Pattern.CASE_INSENSITIVE);
        return pattern.matcher(line).find();
    }

    private boolean isQuery(String line) {
        var pattern = Pattern.compile(selectRegex, Pattern.CASE_INSENSITIVE);
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
