package queryprocessor.evaluator;

import pkb.ProgramKnowledgeBaseAPI;
import pkb.ast.ProcedureNode;
import pkb.ast.abstraction.ASTNode;
import pkb.ast.abstraction.StatementNode;
import queryprocessor.evaluator.abstraction.EvaluationEngine;
import utils.Pair;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;


public class EvalEngine implements EvaluationEngine
{
    private final ProgramKnowledgeBaseAPI api;

    public EvalEngine(ProgramKnowledgeBaseAPI api) {
        this.api = api;
    }

    @Override
    public Set<Pair<ASTNode, ASTNode>> evaluateParentRel(Set<ASTNode> parentCandidates, Set<ASTNode> childCandidates) {
        Set<Pair<ASTNode, ASTNode>> pairs = new HashSet<>();
        //parentCandidates = parentCandidates.stream().filter(p -> !childCandidates.contains(p)).collect(Collectors.toSet());

        for (var cCandidate: childCandidates) {
            if(cCandidate.getParent() == null)
                continue;

            var parent = cCandidate.getParent();
            if(parentCandidates.contains(parent) && parent != cCandidate)
                pairs.add(new Pair<>(parent, cCandidate));
        }

        return pairs;
    }

    @Override
    public Set<Pair<ASTNode, ASTNode>> evaluateParentTransitiveRel(Set<ASTNode> parentCandidates, Set<ASTNode> childCandidates) {
        Set<Pair<ASTNode, ASTNode>> pairs = new HashSet<>();

        for (var childCandidate: childCandidates) {
            var parent = childCandidate.getParent();
            while (parent != null) {
                if(parentCandidates.contains(parent))
                    pairs.add(new Pair<>(parent, childCandidate));

                parent = parent.getParent();
            }
        }

        return pairs;
    }

    @Override
    public Set<Pair<ASTNode, ASTNode>> evaluateUsesRel(Set<ASTNode> statements, Set<ASTNode> variables) {
        Set<Pair<ASTNode, ASTNode>> pairSet = new HashSet<>();

        for (var statement: statements) {
            var uses = api.getUses(statement);

            for (var variable: variables)
            {
               if(uses.contains(variable))
                    pairSet.add(new Pair<>(statement, variable));
            }
        }

        return pairSet;
    }

    @Override
    public Set<Pair<ASTNode, ASTNode>> evaluateModifiesRel(Set<ASTNode> statements, Set<ASTNode> variables) {
        Set<Pair<ASTNode, ASTNode>> pairSet = new HashSet<>();

        for (var statement: statements) {
            var modifies = api.getModifies(statement);

            for (var variable: variables) {
                if(modifies.contains(variable)) {
                    pairSet.add(new Pair<>(statement, variable));
                }
            }
        }

        return pairSet;
    }

    @Override
    public Set<Pair<ASTNode, ASTNode>> evaluateCallsRel(Set<ASTNode> callingCandidate, Set<ASTNode> beingCalledCandidate) {
        Set<Pair<ASTNode, ASTNode>> pairSet = new HashSet<>();

        for (var caller: callingCandidate) {
            Set<ProcedureNode> calledProcedures = api.getCalls(caller);

            for (var called: beingCalledCandidate) {
                if(calledProcedures.contains(called) && caller != called)
                    pairSet.add(new Pair<>(caller, called));
            }
        }

        return pairSet;
    }

    @Override
    public Set<Pair<ASTNode, ASTNode>> evaluateFollowsRel(Set<ASTNode> precedingCandidate, Set<ASTNode> followingCandidate) {
        Set<Pair<ASTNode, ASTNode>> pairSet = new HashSet<>();

        for (var pre: precedingCandidate) {
            for (var following: followingCandidate) {
                if(pre.getRightSibling() == following )
                {
                    pairSet.add(new Pair<>(pre, following));
                    break;
                }
            }
        }

        return pairSet;
    }

    @Override
    public Set<Pair<ASTNode, ASTNode>> evaluateFollowsTransitiveRel(Set<ASTNode> precedingCandidate, Set<ASTNode> followingCandidate)
    {
        Set<Pair<ASTNode, ASTNode>> pairSet = new HashSet<>();

        for (var pre: precedingCandidate) {
            Stack<ASTNode> stack = new Stack<>();
            for (var following: followingCandidate) {
               var node = pre.getRightSibling();

                do {
                    if(node == null) {
                        if(!stack.empty())
                            node = stack.pop();
                        continue;
                    }
                    stack.add(node.getRightSibling());

                    if(node instanceof StatementNode && followingCandidate.contains(node))
                        pairSet.add(new Pair<>(pre, node));

                    node = node.getRightSibling();
                } while(!stack.empty() || node != null);
            }
        }

        return pairSet;
    }

    @Override
    public Set<Pair<ASTNode, ASTNode>> evaluateNextRel(Set<ASTNode> next1, Set<ASTNode> next2)
    {
        Set<Pair<ASTNode, ASTNode>> pairSet = new HashSet<>();

        for (var progLine: next1) {
            var nextProgLine = progLine.getRightSibling();

            if(nextProgLine != null && next2.contains(nextProgLine)) {
                pairSet.add(new Pair<>(progLine, nextProgLine));
            }
        }

        return pairSet;
    }

    @Override
    public Set<Pair<ASTNode, ASTNode>> evaluateNextTransitiveRel(Set<ASTNode> precedingProgLine, Set<ASTNode> followingProgLine) {
        return Collections.emptySet();
    }

    @Override
    public Set<Pair<ASTNode, ASTNode>> evaluateAffectRel(Set<ASTNode> set1, Set<ASTNode> set2) {
        return Collections.emptySet();
    }
}
