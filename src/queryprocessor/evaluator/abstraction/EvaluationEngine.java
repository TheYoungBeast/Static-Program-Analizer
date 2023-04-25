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

    /**
     * Evaluation method for Calls relationship
     * @param callingCandidate List of ASTNodes (Procedures) (candidates that calls another procedure)
     * @param beingCalledCandidate List of ASTNodes (Procedures) (candidates that are being called by another procedure)
     * @return List of pairs of nodes in Calls relationship
     */
    Set<Pair<ASTNode, ASTNode>> evaluateCallsRel(Set<ASTNode> callingCandidate, Set<ASTNode> beingCalledCandidate);

    /**
     * Evaluation method for Follows relationship
     * @param precedingCandidate List of ASTNodes (Statements) (candidates that directly precede another statements)
     * @param followingCandidate List of ASTNodes (Statements) (candidates that directly follow another statements)
     * @return List of pairs of nodes in Follows relationship
     */
    Set<Pair<ASTNode, ASTNode>> evaluateFollowsRel(Set<ASTNode> precedingCandidate, Set<ASTNode> followingCandidate);

    /**
     * Evaluation method for transitive Follows relationship
     * @param precedingCandidate List of ASTNodes (Statements) (candidates that directly precede another statements)
     * @param followingCandidate List of ASTNodes (Statements) (candidates that directly follow another statements)
     * @return List of pairs of nodes in transitive Follows relationship
     */
    Set<Pair<ASTNode, ASTNode>> evaluateFollowsTransitiveRel(Set<ASTNode> precedingCandidate, Set<ASTNode> followingCandidate);

    /**
     * Evaluation method for Next relationship
     * @param precedingProgLine List of ASTNodes (Statements) (candidates that directly precede another statements)
     * @param followingProgLine List of ASTNodes (Statements) (candidates that directly follow another statements)
     * @return List of pairs of nodes in Next relationship
     */
    Set<Pair<ASTNode, ASTNode>> evaluateNextRel(Set<ASTNode> precedingProgLine, Set<ASTNode> followingProgLine);

    /**
     * Evaluation method for transitive Next relationship
     * @param precedingProgLine List of ASTNodes (Statements) (candidates - program lines)
     * @param followingProgLine List of ASTNodes (Statements) (candidates - program lines)
     * @return List of pairs of nodes in transitive Next relationship
     */
    Set<Pair<ASTNode, ASTNode>> evaluateNextTransitiveRel(Set<ASTNode> precedingProgLine, Set<ASTNode> followingProgLine);

    Set<Pair<ASTNode, ASTNode>> evaluateAffectRel(Set<ASTNode> set1, Set<ASTNode> set2);
}
