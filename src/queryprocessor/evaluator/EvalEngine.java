package queryprocessor.evaluator;

import pkb.ast.abstraction.ASTNode;
import utils.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class EvalEngine implements EvaluationEngine
{
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
                    while (nextParent != null) {
                        pairs.add(new Pair<>(nextParent, cCandidate));
                        nextParent = nextParent.getParent();
                    }
                }
            }
        }

        return new ArrayList(pairs);
    }
}
