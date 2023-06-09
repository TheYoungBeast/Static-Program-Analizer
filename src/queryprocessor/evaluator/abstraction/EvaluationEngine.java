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
     * @return Set of pairs of nodes in parent-child relationship
     */
    Set<Pair<ASTNode, ASTNode>> evaluateParentRel(Set<ASTNode> parentCandidates, Set<ASTNode> childCandidates);

    /**
     * Evaluation method for transitive parenthood
     * @param parentCandidates List of ASTNodes (candidates for parent)
     * @param childCandidates List of ASTNodes (candidates for child)
     * @return Set of pairs of nodes in transitive parent-child relationship
     */
    Set<Pair<ASTNode, ASTNode>> evaluateParentTransitiveRel(Set<ASTNode> parentCandidates, Set<ASTNode> childCandidates);

    /**
     * Evaluation method for Uses relationship
     * @param statements List of ASTNodes (candidates that uses)
     * @param variables List of ASTNodes (candidates that are being used)
     * @return Set of pairs of nodes in Uses relationship
     */
    Set<Pair<ASTNode, ASTNode>> evaluateUsesRel(Set<ASTNode> statements, Set<ASTNode> variables);

    /**
     * Evaluation method for Modifies relationship
     * @param statements List of ASTNodes (candidates that modifies)
     * @param variables List of ASTNodes (candidates that are being modified)
     * @return Set of pairs of nodes in Modifies relationship
     */
    Set<Pair<ASTNode, ASTNode>> evaluateModifiesRel(Set<ASTNode> statements, Set<ASTNode> variables);

    /**
     * Evaluation method for Calls relationship
     * @param callingCandidate List of ASTNodes (Procedures) (candidates that calls another procedure)
     * @param beingCalledCandidate List of ASTNodes (Procedures) (candidates that are being called by another procedure)
     * @return Set of pairs of nodes in Calls relationship
     */
    Set<Pair<ASTNode, ASTNode>> evaluateCallsRel(Set<ASTNode> callingCandidate, Set<ASTNode> beingCalledCandidate);

    /**
     * Evaluation method for Transitive Calls relationship
     * @param callersCandidates List of ASTNodes (Procedures) (candidates that calls another procedure)
     * @param calledCandidates List of ASTNodes (Procedures) (candidates that are being called by another procedure)
     * @return Set of pairs of nodes in Transitive Calls relationship
     */
    Set<Pair<ASTNode, ASTNode>> evaluateCallsTransitivesRel(Set<ASTNode> callersCandidates, Set<ASTNode> calledCandidates);

    /**
     * Evaluation method for Follows relationship
     * @param precedingCandidate List of ASTNodes (Statements) (candidates that directly precede another statements)
     * @param followingCandidate List of ASTNodes (Statements) (candidates that directly follow another statements)
     * @return Set of pairs of nodes in Follows relationship
     */
    Set<Pair<ASTNode, ASTNode>> evaluateFollowsRel(Set<ASTNode> precedingCandidate, Set<ASTNode> followingCandidate);

    /**
     * Evaluation method for transitive Follows relationship
     * @param precedingCandidate List of ASTNodes (Statements) (candidates that directly precede another statements)
     * @param followingCandidate List of ASTNodes (Statements) (candidates that directly follow another statements)
     * @return Set of pairs of nodes in transitive Follows relationship
     */
    Set<Pair<ASTNode, ASTNode>> evaluateFollowsTransitiveRel(Set<ASTNode> precedingCandidate, Set<ASTNode> followingCandidate);

    /**
     * Evaluation method for Next relationship
     * @param precedingProgramLine List of ASTNodes (Statements) (candidates that directly precede another statements)
     * @param followingProgramLine List of ASTNodes (Statements) (candidates that directly follow another statements)
     * @return Set of pairs of nodes in Next relationship
     */
    Set<Pair<ASTNode, ASTNode>> evaluateNextRel(Set<ASTNode> precedingProgramLine, Set<ASTNode> followingProgramLine);

    /**
     * Evaluation method for transitive Next relationship
     * @param precedingProgramLine List of ASTNodes (Statements) (candidates - program lines)
     * @param followingProgramLine List of ASTNodes (Statements) (candidates - program lines)
     * @return Set of pairs of nodes in transitive Next relationship
     */
    Set<Pair<ASTNode, ASTNode>> evaluateNextTransitiveRel(Set<ASTNode> precedingProgramLine, Set<ASTNode> followingProgramLine);

    /**
     * Evaluation method for Affect relationship
     * @param assign1Candidates List of ASTNodes (assignments)
     * @param assign2Candidates List of ASTNodes (assignments)
     * @return Set of pairs of nodes in Affects relationship
     */
    Set<Pair<ASTNode, ASTNode>> evaluateAffectRel(Set<ASTNode> assign1Candidates, Set<ASTNode> assign2Candidates);

    /**
     * Evaluation method for transitive Affect relationship
     * @param assign1Candidates List of ASTNodes (assignments)
     * @param assign2Candidates List of ASTNodes (assignments)
     * @return Set of pairs of nodes in transitive Affects relationship
     */
    Set<Pair<ASTNode, ASTNode>> evaluateAffectTransitiveRel(Set<ASTNode> assign1Candidates, Set<ASTNode> assign2Candidates);
}
