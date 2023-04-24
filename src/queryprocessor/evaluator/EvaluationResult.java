package queryprocessor.evaluator;

import pkb.ast.abstraction.ASTNode;
import queryprocessor.preprocessor.synonyms.Synonym;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class EvaluationResult
{
    private final Map<Synonym<?>, Function<ASTNode, String>> resultExtractors;
    private final List<PartialResult> partialResults;

    public EvaluationResult(Map<Synonym<?>, Function<ASTNode, String>> extractors, List<PartialResult> partialResults) {
        this.resultExtractors = extractors;
        this.partialResults = partialResults;
    }

    public Map<Synonym<?>, Function<ASTNode, String>> getExtractors() {
        return resultExtractors;
    }

    public List<PartialResult> getPartialResults() {
        return partialResults;
    }
}
