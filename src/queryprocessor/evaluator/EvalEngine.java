package queryprocessor.evaluator;

import pkb.ProgramKnowledgeBaseAPI;
import pkb.ast.abstraction.ASTNode;
import utils.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EvalEngine implements EvaluationEngine
{
    private final ProgramKnowledgeBaseAPI api;

    public EvalEngine(ProgramKnowledgeBaseAPI api) {
        this.api = api;
    }

    @Override
    public List<Pair<ASTNode, ASTNode>> evaluateParentRel(List<ASTNode> parentCandidates, List<ASTNode> childCandidates) {
        var pairs = new ArrayList<Pair<ASTNode, ASTNode>>();

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
    public List<Pair<ASTNode, ASTNode>> evaluateParentTransitiveRel(List<ASTNode> parentCandidates, List<ASTNode> childCandidates) {
        var pairs = new HashSet<Pair<ASTNode, ASTNode>>();

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

        return new ArrayList<>(pairs);
    }

    @Override
    public List<Pair<ASTNode, ASTNode>> evaluateUsesRel(List<ASTNode> statements, List<ASTNode> variables) {
        Set<Pair<ASTNode, ASTNode>> pairSet = new HashSet<>();

        for (var statement: statements) {
            var uses = api.getUses(statement);

            for (var vUse: uses) {
                for (var variable: variables) {
                    if(vUse == variable) {
                        pairSet.add(new Pair<>(statement, variable));
                        break;
                    }
                }
            }
        }

        return new ArrayList<>(pairSet);
    }

    @Override
    public List<Pair<ASTNode, ASTNode>> evaluateModifiesRel(List<ASTNode> statements, List<ASTNode> variables) {
        Set<Pair<ASTNode, ASTNode>> pairSet = new HashSet<>();

        for (var statement: statements) {
            var modifies = api.getModifies(statement);

            for (var vMod: modifies) {
                for (var variable: variables) {
                    if(vMod == variable && vMod.getParent().getParent() == statement) {
                        pairSet.add(new Pair<>(statement, variable));
                        break;
                    }
                }
            }
        }

        return new ArrayList<>(pairSet);
    }
}
