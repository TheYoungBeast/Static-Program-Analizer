package queryprocessor.evaluator;

import pkb.ast.abstraction.ASTNode;
import queryprocessor.preprocessor.Synonym;

import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

public class EvaluationResult
{
    private final HashMap<Synonym<?>, List<ASTNode>> LUT;
    private final HashMap<Synonym<?>, Function<ASTNode, String>> resultExtractors;

    public EvaluationResult(HashMap<Synonym<?>, List<ASTNode>> LUT, HashMap<Synonym<?>, Function<ASTNode, String>> extractors) {
        this.LUT = LUT;
        this.resultExtractors = extractors;
    }

    public HashMap<Synonym<?>, List<ASTNode>> getLUT() {
        return LUT;
    }

    public HashMap<Synonym<?>, Function<ASTNode, String>> getExtractors() {
        return resultExtractors;
    }
}
