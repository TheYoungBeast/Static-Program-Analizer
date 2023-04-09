package queryresultprojector;

import queryprocessor.evaluator.EvaluationResult;

import java.util.Map;
import java.util.stream.Collectors;


public class QueryResultProjector
{
    private EvaluationResult evaluationResult;

    public void setEvaluationResult(EvaluationResult result) {
        this.evaluationResult = result;
    }

    public String format() {
        var builder = new StringBuilder();

        var LUT = evaluationResult.getLUT();
        var extractorMap = evaluationResult.getExtractors();
        var synonyms = extractorMap.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList());

        for (var synonym: synonyms) {
            var results = LUT.get(synonym);
            var extractor = extractorMap.get(synonym);

            for (var result: results) {
                builder.append(String.format("%s: %s", synonym.getKeyword().getName(), extractor.apply(result)));
                builder.append(System.getProperty("line.separator"));
            }
        }

        return builder.toString();
    }
}
