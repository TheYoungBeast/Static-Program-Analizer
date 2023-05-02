package queryprocessor.preprocessor.extractors;

import queryprocessor.preprocessor.Keyword;
import queryprocessor.preprocessor.ParsingProgress;
import queryprocessor.preprocessor.QueryPreprocessor;
import queryprocessor.preprocessor.exceptions.InvalidQueryException;
import queryprocessor.querytree.ResBooleanNode;
import queryprocessor.querytree.ResNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ResultSynonymExtractor extends Extractor {
    private final QueryPreprocessor queryPreprocessor;
    private final ParsingProgress parsingProgress;

    public ResultSynonymExtractor(QueryPreprocessor queryPreprocessorBase, ParsingProgress parsingProgress) {
        super(parsingProgress);
        this.queryPreprocessor = queryPreprocessorBase;
        this.parsingProgress = parsingProgress;
    }

    public List<ResNode> extractSynonyms(String query) throws InvalidQueryException {
        var resultNodes = new ArrayList<ResNode>();

        var matcher = Pattern.compile(Keyword.SYNONYMS.getRegExpr(), Pattern.CASE_INSENSITIVE).matcher(query);

        if (!matcher.find())
            return Collections.emptyList();

        var group = matcher.group().trim();

        parsingProgress.setParsed(matcher.toMatchResult().start(), matcher.toMatchResult().end());

        List<String> identifiers = new ArrayList<>();
        if (group.contains(",")) {
            if (!group.matches(Keyword.RESULT_TUPLE.getRegExpr()))
                throw new InvalidQueryException("Invalid result tuple format. Expected e.g. <res1, res2, ...>", group);

            var resTuple = group.replaceAll("[<>]", "");
            identifiers = Arrays.stream(resTuple.split(",")).map(String::trim).collect(Collectors.toList());
        } else {
            if (group.isBlank())
                return Collections.emptyList();

            var booleanMatcher = Pattern.compile(Keyword.BOOLEAN.getRegExpr(), Pattern.CASE_INSENSITIVE).matcher(group);

            if (booleanMatcher.find())
                resultNodes.add(new ResBooleanNode());
            else
                identifiers.add(group.trim());
        }

        for (var id : identifiers) {
            var synonym = queryPreprocessor.getDeclaredSynonym(id);
            if (synonym == null)
                throw new InvalidQueryException(String.format("Undeclared synonym %s in the select clause", id), group);

            resultNodes.add(new ResNode(synonym));
        }

        return resultNodes;
    }
}
