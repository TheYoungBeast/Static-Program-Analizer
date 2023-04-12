package queryprocessor.evaluator;

import pkb.ast.abstraction.ASTNode;
import utils.Pair;

import java.util.List;

public interface EvaluationEngine
{
    /**
     * Evaluation method for parenthood
     * @param parentCandidates List of ASTNodes (candidates for parent)
     * @param childCandidates List of ASTNodes (candidates for child)
     * @return List of pairs of nodes in parent-child relationship
     */
    List<Pair<ASTNode, ASTNode>> evaluateParentRel(List<ASTNode> parentCandidates, List<ASTNode> childCandidates);

    /**
     * Evaluation method for transitive parenthood
     * @param parentCandidates List of ASTNodes (candidates for parent)
     * @param childCandidates List of ASTNodes (candidates for child)
     * @return List of pairs of nodes in transitive parent-child relationship
     */
    List<Pair<ASTNode, ASTNode>> evaluateParentTransitiveRel(List<ASTNode> parentCandidates, List<ASTNode> childCandidates);
}
