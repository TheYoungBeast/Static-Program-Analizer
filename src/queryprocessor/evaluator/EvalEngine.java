package queryprocessor.evaluator;

import pkb.ProgramKnowledgeBaseAPI;
import pkb.ast.CallNode;
import pkb.ast.abstraction.ASTNode;
import queryprocessor.evaluator.abstraction.EvaluationEngine;
import utils.Pair;


import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class EvalEngine implements EvaluationEngine
{
    private final ProgramKnowledgeBaseAPI api;

    public EvalEngine(ProgramKnowledgeBaseAPI api) {
        this.api = api;
    }

    @Override
    public Set<Pair<ASTNode, ASTNode>> evaluateParentRel(Set<ASTNode> parentCandidates, Set<ASTNode> childCandidates) {
        Set<Pair<ASTNode, ASTNode>> pairs = new HashSet<>();
        childCandidates = childCandidates.stream().filter(c -> !parentCandidates.contains(c)).collect(Collectors.toSet());

        for (var cCandidate: childCandidates) {
            if(cCandidate.getParent() == null)
                continue;

            var parent = cCandidate.getParent();
            for (var pCandidate: parentCandidates) {
                if(parent == pCandidate) {
                    pairs.add(new Pair<>(pCandidate, cCandidate));
                    break;
                }
            }
        }

        return pairs;
    }

    @Override
    public Set<Pair<ASTNode, ASTNode>> evaluateParentTransitiveRel(Set<ASTNode> parentCandidates, Set<ASTNode> childCandidates) {
        Set<Pair<ASTNode, ASTNode>> pairs = new HashSet<>();

        for (var cCandidate: childCandidates) {
            if(cCandidate.getParent() == null)
                continue;

            var parent = cCandidate.getParent();
            for (var pCandidate: parentCandidates) {
                if(parent == pCandidate)
                {
                    pairs.add(new Pair<>(pCandidate, cCandidate));

                    var nextParent = pCandidate.getParent();
                    // rodzic rodzica musi byc tego samego typu co kandydat na rodzica
                    while (nextParent != null && nextParent.getClass().equals(pCandidate.getClass()))
                    {
                        pairs.add(new Pair<>(nextParent, cCandidate));
                        nextParent = nextParent.getParent();
                    }
                }
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
                    break;
                }
            }
        }

        return pairSet;
    }

    @Override
    public Set<Pair<ASTNode, ASTNode>> evaluateCallsRel(Set<ASTNode> callingCandidate, Set<ASTNode> beingCalledCandidate) {
        Set<Pair<ASTNode, ASTNode>> pairSet = new HashSet<>();

        for (var caller: callingCandidate) {
            Set<CallNode> calledProcedures = null;

            for (var called: beingCalledCandidate) {
                if(calledProcedures.contains(called))
                    pairSet.add(new Pair<>(caller, called));
            }
        }

        return pairSet;
    }
}
