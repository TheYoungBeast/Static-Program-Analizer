package queryprocessor.evaluator;

import pkb.ast.abstraction.ASTNode;
import queryprocessor.preprocessor.Synonym;

import java.util.HashMap;
import java.util.Set;
import java.util.function.Function;

public class EvaluationResult
{
    private final HashMap<Synonym<?>, Set<ASTNode>> LUT;
    private final HashMap<Synonym<?>, Function<ASTNode, String>> resultExtractors;

    public EvaluationResult(HashMap<Synonym<?>, Set<ASTNode>> LUT, HashMap<Synonym<?>, Function<ASTNode, String>> extractors) {
        this.LUT = LUT;
        this.resultExtractors = extractors;
    }

    public HashMap<Synonym<?>, Set<ASTNode>> getLUT() {
        return LUT;
    }

    public HashMap<Synonym<?>, Function<ASTNode, String>> getExtractors() {
        return resultExtractors;
    }
}
