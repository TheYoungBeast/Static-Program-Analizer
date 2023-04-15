package queryprocessor.evaluator.abstraction;

import pkb.ast.abstraction.ASTNode;
import utils.Pair;

import java.util.Set;

public interface EvaluationEngine
{
    /**
     * Evaluation method for parenthood
     * @param parentCandidates List of ASTNodes (candidates for parent)
     * @param childCandidates List of ASTNodes (candidates for child)
     * @return List of pairs of nodes in parent-child relationship
     */
    Set<Pair<ASTNode, ASTNode>> evaluateParentRel(Set<ASTNode> parentCandidates, Set<ASTNode> childCandidates);

    /**
     * Evaluation method for transitive parenthood
     * @param parentCandidates List of ASTNodes (candidates for parent)
     * @param childCandidates List of ASTNodes (candidates for child)
     * @return List of pairs of nodes in transitive parent-child relationship
     */
    Set<Pair<ASTNode, ASTNode>> evaluateParentTransitiveRel(Set<ASTNode> parentCandidates, Set<ASTNode> childCandidates);

    /**
     * Evaluation method for Uses relationship
     * @param statements List of ASTNodes (candidates that uses)
     * @param variables List of ASTNodes (candidates that are being used)
     * @return List of pairs of nodes in Uses relationship
     */
    Set<Pair<ASTNode, ASTNode>> evaluateUsesRel(Set<ASTNode> statements, Set<ASTNode> variables);

    /**
     * Evaluation method for Modifies relationship
     * @param statements List of ASTNodes (candidates that modifies)
     * @param variables List of ASTNodes (candidates that are being modified)
     * @return List of pairs of nodes in Modifies relationship
     */
    Set<Pair<ASTNode, ASTNode>> evaluateModifiesRel(Set<ASTNode> statements, Set<ASTNode> variables);
}
