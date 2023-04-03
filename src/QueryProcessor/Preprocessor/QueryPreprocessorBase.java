package QueryProcessor.Preprocessor;

import QueryProcessor.Preprocessor.Exceptions.*;
import QueryProcessor.QueryTree.QueryTree;
import QueryProcessor.QueryTree.ResNode;
import QueryProcessor.QueryTree.ResultNode;
import QueryProcessor.QueryTree.QueryTreeImpl;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class QueryPreprocessorBase implements QueryPreprocessor
{
    public final List<String> keywords;
    public final HashSet<Synonym> synonymsList;

    private static final String selectRegex = "select(\\s*)[a-zA-Z]+[0-9]*(\\s*)(,*(\\s*)[a-zA-Z]*[0-9]*)*";

    public QueryPreprocessorBase() {
        this.synonymsList = new HashSet<>();
        this.keywords = Arrays.stream(Keyword.values())
                .map(k -> k.getPattern())
                .collect(Collectors.toList());
    }

    public QueryTree parseQuery(String query) throws InvalidQueryException, MissingArgumentException
    {
        if(query.length() == 0)
            throw new InvalidQueryException("Empty query");

        var array = query.split(";");

        return parseQuery(array);
    }

    public QueryTree parseQuery(String[] querylines) throws InvalidQueryException, MissingArgumentException
    {
        if(querylines.length == 0)
            throw new InvalidQueryException("The query not properly ended. ';' is missing", 1);

        boolean selectClause = false;
        for (int i = 0; i < querylines.length; i++) {
            var line = querylines[i];

            if(line.length() == 0)
                continue;

            // Does the current query's line contain a select clause?
            var pattern = Pattern.compile(selectRegex, Pattern.CASE_INSENSITIVE);

            if(pattern.matcher(line).find()) {
                if (selectClause)
                    throw new InvalidQueryException("Multiple select clauses", i, line);
                else
                    selectClause = true;
            }

            // Synonyms declarations
            if(!selectClause)
            {
                Keyword type = null;
                int start = 0;

                var set = new HashSet<Keyword>(
                        Arrays.asList(
                                Keyword.WHILE,
                                Keyword.ASSIGN,
                                Keyword.STATEMENT)
                );

                int cnt = 0;
                for (Keyword keyword: set)
                {
                    var matcher = Pattern.compile(Pattern.quote(keyword.getPattern()), Pattern.CASE_INSENSITIVE).matcher(line);

                    if(matcher.find()) {
                        type = keyword;
                        start = matcher.end();
                        cnt++;
                    }
                }

                if(cnt > 1)
                    throw new InvalidQueryException("Multiple synonym types in one declaration", i, line);

                if(type == null)
                    throw new InvalidQueryException("Unrecognized synonym definition", i, line);

                var synonyms = Pattern.compile(Keyword.SYNONYM.getPattern())
                        .matcher(line.substring(start))
                        .results()
                        .map(mr -> mr.group())
                        .collect(Collectors.toList());

                if(synonyms.isEmpty())
                    throw new InvalidQueryException("Missing synonym identifier", i, line);

                for (String synonym: synonyms) {
                    var s = new Synonym(synonym, type);
                    if(synonymsList.contains(s))
                        throw new InvalidQueryException("Synonym already declared", i, synonym);
                    else
                        synonymsList.add(s);
                }
            }
            // Select clause
            else
            {
                var matcher = Pattern.compile(Keyword.SELECT.getPattern(), Pattern.CASE_INSENSITIVE).matcher(line);

                var start = 0;
                if(matcher.find())
                    start = matcher.end();
                else
                    throw new UnknownError();

                // synonym list not empty means - 'select clause' should contain only those declared synonyms
                if(!synonymsList.isEmpty()) {
                    var selectVars = Pattern.compile(Keyword.SYNONYM.getPattern())
                            .matcher(line.substring(start))
                            .results()
                            .map(mr -> mr.group())
                            .collect(Collectors.toList());

                    boolean found = false;
                    for (String v: selectVars) {
                        for (Synonym s: synonymsList) {
                            if(v.equals(s.getIdentifier())) {
                                found = true;
                                break;
                            }
                        }

                        if(!found)
                            throw new InvalidQueryException("Variable " + v + " in select clause is out of declared synonyms");
                        else
                            found = false;
                    }


                    var rNode = new ResultNode();

                    boolean first = true;
                    ResNode last = null;

                    for (String v: selectVars) {

                        var resNode = new ResNode(v);
                        if(first) {
                            resNode.setUp(rNode);
                            rNode.SetFirstChild(resNode);
                            first = false;
                        }
                        else {
                            resNode.setUp(last);
                            last.SetRightSibling(resNode);
                        }

                        last = resNode;
                    }

                    return new QueryTreeImpl(rNode);
                }
            }
        }

        if(!selectClause)
            throw new InvalidQueryException("No select clause was found");

        return new QueryTreeImpl(null) {};
    }
}
