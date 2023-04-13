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

    /**
     * Evaluation method for Uses relationship
     * @param statements List of ASTNodes (candidates that uses)
     * @param variables List of ASTNodes (candidates that are being used)
     * @return List of pairs of nodes in Uses relationship
     */
    List<Pair<ASTNode, ASTNode>> evaluateUsesRel(List<ASTNode> statements, List<ASTNode> variables);

    /**
     * Evaluation method for Modifies relationship
     * @param statements List of ASTNodes (candidates that modifies)
     * @param variables List of ASTNodes (candidates that are being modified)
     * @return List of pairs of nodes in Modifies relationship
     */
    List<Pair<ASTNode, ASTNode>> evaluateModifiesRel(List<ASTNode> statements, List<ASTNode> variables);
}
