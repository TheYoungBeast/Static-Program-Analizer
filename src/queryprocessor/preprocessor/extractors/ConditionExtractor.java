package queryprocessor.preprocessor.extractors;

import queryprocessor.preprocessor.Keyword;
import queryprocessor.preprocessor.ParsingProgress;
import queryprocessor.preprocessor.QueryPreprocessor;
import queryprocessor.preprocessor.exceptions.InvalidQueryException;
import queryprocessor.preprocessor.synonyms.Synonym;
import queryprocessor.querytree.*;
import utils.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ConditionExtractor {
    private final QueryPreprocessor queryPreprocessor;
    private final ParsingProgress parsingProgress;

    public ConditionExtractor(QueryPreprocessor queryPreprocessor, ParsingProgress parsingProgress) {
        this.queryPreprocessor = queryPreprocessor;
        this.parsingProgress = parsingProgress;
    }

    public List<Condition> extractConditions(String query) throws InvalidQueryException {
        var conditions = new ArrayList<Condition>();

        var matcher = Pattern.compile(Keyword.WITH_CLAUSE.getRegExpr(), Pattern.CASE_INSENSITIVE).matcher(query);
        var matchResults = matcher.results().collect(Collectors.toList());

        for (var match : matchResults) {
            var result = match.group();
            conditions.addAll(extractAttributes(result));
            parsingProgress.setParsed(match.start(), match.end());
        }

        return conditions;
    }

    public List<Condition> extractAttributes(String group) throws InvalidQueryException {
        var conditionPart = Arrays.stream(group.split("=")).map(String::trim).collect(Collectors.toList());
        conditionPart.set(0, conditionPart.get(0).replaceAll(Keyword.WITH.getRegExpr(), "").trim());

        List<Condition> conditionNodes = new ArrayList<>();
        List<Pair<Synonym<?>, AttrName>> refs = new ArrayList<>();

        for (var c : conditionPart) {
            if (!c.contains("."))
                continue;

            var attrs = Arrays.stream(c.split("\\.")).map(String::trim).collect(Collectors.toList());

            var synonym = queryPreprocessor.getDeclaredSynonym(attrs.get(0));
            var attr = findAttrName(attrs.get(1));

            if (synonym == null)
                throw new InvalidQueryException(String.format("Unrecognized synonym %s", attrs.get(0)), group);

            if (attr == null)
                throw new InvalidQueryException(String.format("Unrecognized attribute %s", attrs.get(1)), group);

            refs.add(new Pair<>(synonym, attr));
        }

        if (refs.size() == 1) {
            var value = conditionPart.get(1);

            if (value.contains("\""))
                value = value.replaceAll("\"", "");

            var pair = refs.get(0);
            var attrRef = new AttrRef(pair.getFirst(), pair.getSecond());
            var attrVal = new AttrValue(value);
            conditionNodes.add(new ConditionRefValue(attrRef, attrVal));
        } else {
            var ref1 = new AttrRef(refs.get(0).getFirst(), refs.get(0).getSecond());
            var ref2 = new AttrRef(refs.get(1).getFirst(), refs.get(1).getSecond());
            conditionNodes.add(new ConditionRefRef(ref1, ref2));
        }

        return conditionNodes;
    }

    public static AttrName findAttrName(String name) {
        for (var attr: AttrName.values()) {
            if(name.equals(attr.getName()))
                return attr;
        }

        return null;
    }
}
