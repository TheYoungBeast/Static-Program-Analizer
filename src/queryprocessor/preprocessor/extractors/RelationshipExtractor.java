package queryprocessor.preprocessor.extractors;

import queryprocessor.preprocessor.ArgumentTypeDeducer;
import queryprocessor.preprocessor.Keyword;
import queryprocessor.preprocessor.ParsingProgress;
import queryprocessor.preprocessor.QueryPreprocessorBase;
import queryprocessor.preprocessor.QueryPreprocessor;
import queryprocessor.preprocessor.exceptions.InvalidQueryException;
import queryprocessor.preprocessor.exceptions.MissingArgumentException;
import queryprocessor.preprocessor.synonyms.StatementIdSynonym;
import queryprocessor.preprocessor.synonyms.Synonym;
import queryprocessor.querytree.RelationshipRef;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RelationshipExtractor {
    private final QueryPreprocessor queryPreprocessor;
    private final ParsingProgress parsingProgress;

    public RelationshipExtractor(QueryPreprocessor queryPreprocessor, ParsingProgress parsingProgress) {
        this.queryPreprocessor = queryPreprocessor;
        this.parsingProgress = parsingProgress;
    }

    public List<RelationshipRef> extractRelationships(String query) throws InvalidQueryException, MissingArgumentException {
        var relationships = new ArrayList<RelationshipRef>();

        var start = query.length();
        var end = 0;
        for (Keyword rel : QueryPreprocessorBase.relationshipsKeywords) {
            var relMatcher = Pattern.compile(rel.getRegExpr(), Pattern.CASE_INSENSITIVE).matcher(query);
            var matchResults = relMatcher.results().collect(Collectors.toList());

            if (matchResults.isEmpty())
                continue;

            for (var match : matchResults) {
                if (match.start() < start)
                    start = match.start();

                if (match.end() > end)
                    end = match.end();

                var args = extractArguments(query, rel, match.start(), match.end());
                relationships.add(new RelationshipRef(rel, args));
                parsingProgress.setParsed(match.start(), match.end());
            }
        }

        if (!relationships.isEmpty() && (relationships.size() - 1) != getConcatenatorCount(query, start, end))
            throw new InvalidQueryException("Missing 'and' concatenator in between relationships", query);

        return relationships;
    }

    public List<Synonym<?>> extractArguments(String query, Keyword relType, int start, int end) throws MissingArgumentException, InvalidQueryException {
        var argsMatcher = Pattern.compile(Keyword.REL_ARGS.getRegExpr(), Pattern.CASE_INSENSITIVE).matcher(query).region(start, end);

        if (!argsMatcher.find())
            throw new MissingArgumentException(relType.getName(), 0, query);

        var args = argsMatcher.group().split(",");

        var arguments = new ArrayList<Synonym<?>>();
        var argN = 0;
        for (var arg : args) {
            argN++;
            arg = arg.trim();

            Synonym<?> synonym;
            if (arg.contains("\"") || arg.equals(Keyword.PLACEHOLDER.getRegExpr())) {
                var td = new ArgumentTypeDeducer();
                arg = arg.replaceAll("\"", "").trim();
                synonym = td.deduce(relType, arg, argN);
            } else if (isNumeric(arg))
                synonym = new StatementIdSynonym(arg);
            else
                synonym = queryPreprocessor.getDeclaredSynonym(arg.trim());

            if (synonym == null)
                throw new InvalidQueryException("Unrecognized parameter synonym", arg);

            arguments.add(synonym);
        }

        return arguments;
    }

    public long getConcatenatorCount(String line, int start, int end) {
        var matcher = Pattern.compile(Keyword.AND.getRegExpr(), Pattern.CASE_INSENSITIVE).matcher(line).region(start, end);
        var results = matcher.results().collect(Collectors.toList());

        if (!results.isEmpty())
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
