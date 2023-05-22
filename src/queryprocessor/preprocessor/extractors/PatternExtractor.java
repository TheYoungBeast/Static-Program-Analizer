package queryprocessor.preprocessor.extractors;

import frontend.parser.Parser;
import pkb.ast.abstraction.ASTNode;
import queryprocessor.preprocessor.Keyword;
import queryprocessor.preprocessor.ParsingProgress;
import queryprocessor.preprocessor.QueryPreprocessor;
import queryprocessor.preprocessor.exceptions.InvalidQueryException;
import queryprocessor.preprocessor.synonyms.NamedVariableSynonym;
import queryprocessor.preprocessor.synonyms.Synonym;
import queryprocessor.preprocessor.synonyms.SynonymFactory;
import queryprocessor.querytree.ExpressionPattern;
import queryprocessor.querytree.ExpressionPatternIf;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PatternExtractor extends Extractor {
    private final QueryPreprocessor queryPreprocessor;
    private final ParsingProgress parsingProgress;

    public PatternExtractor(QueryPreprocessor queryPreprocessor, ParsingProgress parsingProgress) {
        super(parsingProgress);
        this.queryPreprocessor = queryPreprocessor;
        this.parsingProgress = parsingProgress;
    }

    public List<ExpressionPattern> extractPatterns(String query) throws InvalidQueryException {
        var patterns = new ArrayList<ExpressionPattern>();

        var regions = extractRegions(query, Keyword.PATTERN);

        for (var region: regions)
        {
            var matcher = Pattern.compile(Keyword.PATTERN_COND.getRegExpr(), Pattern.CASE_INSENSITIVE)
                    .matcher(query)
                    .region(region.getFirst(), region.getSecond());
            var results = matcher.results().collect(Collectors.toList());

            for (var matchResult : results) {
                parsingProgress.setParsed(matchResult.start(), matchResult.end());

                var group = matchResult.group().trim();

                // they need to exist because regex guarantees it
                var i1 = group.indexOf('(');
                var i2 = group.indexOf(')');

                var cond = group.substring(i1+1, i2);
                var split = cond.split(",");
                var leftHandExpStr = split[0].trim();
                var rightHandExprStr = split[1].replaceAll(" ", "");

                var synStr = group.substring(0, i1).trim();
                var patternSynonym = queryPreprocessor.getDeclaredSynonym(synStr);

                if (patternSynonym == null)
                    throw new InvalidQueryException(String.format("Unrecognized synonym %s", synStr), group);

                boolean ifPattern = false;
                if(patternSynonym.getKeyword() == Keyword.IF)
                {
                    ifPattern = true;
                    if(split.length != 3)
                        throw new InvalidQueryException("Invalid number of arguments. Expected 3", group);
                }

                Synonym<?> leftHandExp = null;
                if (leftHandExpStr.equals("_"))
                    leftHandExp = SynonymFactory.create(leftHandExpStr, Keyword.VARIABLE);
                else {
                    var matcher1 = Pattern.compile(Keyword.SYNONYM.getRegExpr(), Pattern.CASE_INSENSITIVE).matcher(leftHandExpStr);

                    if(matcher1.find()) {
                        if (leftHandExpStr.contains("\""))
                            leftHandExp = new NamedVariableSynonym(matcher1.group());
                        else
                            leftHandExp = queryPreprocessor.getDeclaredSynonym(leftHandExpStr);
                    }
                }

                if(leftHandExp == null)
                    throw new InvalidQueryException("Unrecognized pattern argument", group);



                var expressions = new ArrayList<String>();
                expressions.add(rightHandExprStr);

                if(ifPattern)
                {
                    var elseExpStr = split[2].replaceAll(" ", "");
                    expressions.add(elseExpStr);
                }

                var expressionPatterns = new ArrayList<ExpressionPattern>();
                for(var sub: expressions) {
                    ASTNode expTree = parseExpression(sub);
                    boolean lookBehind = sub.contains("_\"");
                    boolean lookAhead = sub.contains("\"_");

                    expressionPatterns.add(new ExpressionPattern(patternSynonym, leftHandExp, expTree, lookAhead, lookBehind));
                }

                if(ifPattern) {
                    assert (expressionPatterns.size() == 2);
                    patterns.add(new ExpressionPatternIf(expressionPatterns.get(0), expressionPatterns.get(1)));
                }
                else patterns.add(expressionPatterns.get(0));
            }

            if((results.size()-1) != getConcatenatorCount(query, region.getFirst(), region.getSecond()))
                throw new InvalidQueryException("Missing 'and' concatenator or missing pattern conditions");
        }

        return patterns;
    }

    private ASTNode parseExpression(String rightHandExpr) throws InvalidQueryException
    {
        ASTNode expTree = null;
        try {
            var exp = rightHandExpr.replaceAll("[\"_]", "").trim();
            if(!exp.isBlank())
                expTree = Parser.parsePattern(exp);
        }
        catch (Exception e) {
            throw new InvalidQueryException(String.format("Invalid pattern expression: %s", e.getMessage()));
        }

        return expTree;
    }
}
