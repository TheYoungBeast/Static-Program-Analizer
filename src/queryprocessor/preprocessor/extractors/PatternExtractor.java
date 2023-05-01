package queryprocessor.preprocessor.extractors;

import pkb.ast.abstraction.ASTNode;
import queryprocessor.preprocessor.Keyword;
import queryprocessor.preprocessor.ParsingProgress;
import queryprocessor.preprocessor.QueryPreprocessor;
import queryprocessor.preprocessor.exceptions.InvalidQueryException;
import queryprocessor.preprocessor.synonyms.NamedVariableSynonym;
import queryprocessor.preprocessor.synonyms.Synonym;
import queryprocessor.preprocessor.synonyms.SynonymFactory;
import queryprocessor.querytree.ExpressionPattern;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PatternExtractor {
    private final QueryPreprocessor queryPreprocessor;
    private final ParsingProgress parsingProgress;

    public PatternExtractor(QueryPreprocessor queryPreprocessor, ParsingProgress parsingProgress) {
        this.queryPreprocessor = queryPreprocessor;
        this.parsingProgress = parsingProgress;
    }

    public List<ExpressionPattern> extractPatterns(String query) throws InvalidQueryException {
        var patterns = new ArrayList<ExpressionPattern>();

        var matcher = Pattern.compile(Keyword.PATTERN_COND.getRegExpr(), Pattern.CASE_INSENSITIVE).matcher(query);
        var results = matcher.results().collect(Collectors.toList());

        for (var matchResult : results) {
            parsingProgress.setParsed(matchResult.start(), matchResult.end());

            var group = matchResult.group().trim();

            // they need to exist because regex guarantees it
            var i1 = group.indexOf('(');
            var i2 = group.indexOf(')');

            var cond = group.substring(i1, i2);
            var split = cond.split(",");
            var leftHandExpStr = split[0].trim();
            var rightHandExprStr = split[1].replaceAll(" ", "");

            var synStr = group.substring(0, i1).trim();
            var patternSynonym = queryPreprocessor.getDeclaredSynonym(synStr);

            if (patternSynonym == null)
                throw new InvalidQueryException(String.format("Unrecognized synonym %s", synStr), group);

            Synonym<?> leftHandExp = null;
            if (leftHandExpStr.equals("_"))
                leftHandExp = SynonymFactory.create(leftHandExpStr, Keyword.VARIABLE);
            else {
                var matcher1 = Pattern.compile(Keyword.SYNONYM.getRegExpr(), Pattern.CASE_INSENSITIVE).matcher(leftHandExpStr);
                if (matcher1.find())
                    leftHandExp = new NamedVariableSynonym(matcher1.group());
            }

            // tutaj dac metode parsujacą wyrazenie na drzewko
            ASTNode tree = null;

            boolean lookBehind = rightHandExprStr.contains("_\"");
            boolean lookAhead = rightHandExprStr.contains("\"_");

            patterns.add(new ExpressionPattern(patternSynonym, leftHandExp, tree, lookAhead, lookBehind));
        }

        return patterns;
    }
}
