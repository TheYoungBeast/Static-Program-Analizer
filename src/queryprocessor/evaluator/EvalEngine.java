package queryprocessor.evaluator;

import pkb.ProgramKnowledgeBaseAPI;
import pkb.ast.AssignmentNode;
import pkb.ast.ProcedureNode;
import pkb.ast.WhileNode;
import pkb.ast.abstraction.ASTNode;
import pkb.ast.abstraction.StatementNode;
import pkb.cfg.CfgNode;
import queryprocessor.evaluator.abstraction.EvaluationEngine;
import utils.Pair;

import java.util.*;
import java.util.stream.Collectors;


public class EvalEngine implements EvaluationEngine
{
    private final ProgramKnowledgeBaseAPI pkb;

    public EvalEngine(ProgramKnowledgeBaseAPI api) {
        this.pkb = api;
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
            var uses = pkb.getUses(statement);

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
            var modifies = pkb.getModifies(statement);

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
            Set<ProcedureNode> calledProcedures = pkb.getCalls(caller);

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

        return pairSet;
    }

    @Override
    public Set<Pair<ASTNode, ASTNode>> evaluateNextRel(Set<ASTNode> precedingProgramLine, Set<ASTNode> followingProgramLine)
    {
        Set<Pair<ASTNode, ASTNode>> pairSet = new HashSet<>();

        for (var programLine: precedingProgramLine) {
            var ownerProcedure = getStatementOwner(programLine);

            if(ownerProcedure == null)
                continue;

            var graph = pkb.getControlFlowGraph(ownerProcedure);

            var branching = graph.getBranching(programLine);

            var first = branching.getFirst();
            if(first != null && followingProgramLine.contains(first.getAstNode()))
                pairSet.add(new Pair<>(programLine, first.getAstNode()));

            var second = branching.getSecond();
            if(second != null && followingProgramLine.contains(second.getAstNode()))
                pairSet.add(new Pair<>(programLine, second.getAstNode()));
        }

        return pairSet;
    }

    @Override
    public Set<Pair<ASTNode, ASTNode>> evaluateNextTransitiveRel(Set<ASTNode> precedingProgramLine, Set<ASTNode> followingProgramLine) {
        var resultPairs = new HashSet<Pair<ASTNode, ASTNode>>();

        var preceding = precedingProgramLine.stream().sorted(Comparator.comparingInt(a -> ((StatementNode) a).getStatementId())).collect(Collectors.toList());
        var following = followingProgramLine.stream().sorted((a1, a2) -> ((StatementNode)a2).getStatementId() - ((StatementNode)a1).getStatementId()).collect(Collectors.toList());

        var computed = new HashSet<Pair<ASTNode, ASTNode>>();

        for (var programLine: preceding)
        {
            for (var destination: following)
            {
                if(computed.contains(new Pair<>(programLine, destination)))
                    continue;

                var ownerProcedure = getStatementOwner(programLine);
                if(ownerProcedure == null)
                    continue;

                var controlFlowGraph = pkb.getControlFlowGraph(ownerProcedure);

                var flowPaths = controlFlowGraph.getFlowPaths(programLine, destination);

                for (var path: flowPaths) {
                    var astPath = path.stream().map(CfgNode::getAstNode).collect(Collectors.toList());

                    for (var astNode: astPath)
                    {
                        if(!followingProgramLine.contains(astNode))
                            continue;

                        computed.add(new Pair<>(programLine, astNode));
                        resultPairs.add(new Pair<>(programLine, astNode));
                    }
                }
            }
        }

        return resultPairs;
    }

    @Override
    public Set<Pair<ASTNode, ASTNode>> evaluateAffectRel(Set<ASTNode> assign1, Set<ASTNode> assign2)
    {
        var resultsPairs = new HashSet<Pair<ASTNode, ASTNode>>();

        for (var a1: assign1) {
            if(!(a1 instanceof AssignmentNode))
                continue;

            var modifies = pkb.getModifies(a1);
            if(modifies.isEmpty())
                continue;

            var variable = modifies.stream().findFirst().get();

            var ownerProcedure = getStatementOwner(a1);

            for (var a2: assign2) {
                if(a1 == a2)
                    continue;

                if(!(a2 instanceof AssignmentNode))
                    continue;

                if(!ownerProcedure.equals(getStatementOwner(a2)))
                    continue;

                var uses = pkb.getUses(a2);
                if(uses.isEmpty())
                    continue;

                if(!uses.contains(variable))
                    continue;

                var cfg = pkb.getControlFlowGraph(ownerProcedure);
                if(cfg == null)
                    continue;

                var flowPaths = cfg.getFlowPaths(a1, a2);
                flowPaths = flowPaths.stream().filter(l -> !l.isEmpty()).collect(Collectors.toList());

                if(flowPaths.isEmpty())
                    continue;

                var forbidden = false;
                for (var flowPath: flowPaths) {
                    for (var step: flowPath) {
                        var node = step.getAstNode();
                        if(node == null || node instanceof WhileNode)
                            continue;

                        if(node.equals(a1) || node.equals(a2))
                            continue;

                        var nodeModifies = pkb.getModifies(node);
                        if(nodeModifies.isEmpty())
                            continue;

                        if(nodeModifies.contains(variable)) {
                            forbidden = true;
                            break;
                        }
                    }

                    if(forbidden)
                        break;
                }

                if(forbidden)
                    continue;

                resultsPairs.add(new Pair<>(a1, a2));
            }
        }

        return resultsPairs;
    }

    private ProcedureNode getStatementOwner(ASTNode node)
    {
        while (node != null) {
            if(node instanceof ProcedureNode)
                return (ProcedureNode) node;

            node = node.getParent();
        }

        return null;
    }
}
