package queryprocessor.evaluator;

import pkb.ast.abstraction.ASTNode;
import pkb.ProgramKnowledgeBaseAPI;
import queryprocessor.preprocessor.Keyword;
import queryprocessor.preprocessor.Synonym;
import queryprocessor.querytree.ConditionNode;
import queryprocessor.querytree.QueryTree;
import queryprocessor.querytree.RelationshipRef;
import queryprocessor.querytree.ResNode;
import utils.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;
import java.util.function.Function;
import java.util.stream.Collectors;

public class QueryEvaluatorBase implements QueryEvaluator
{
    public final ProgramKnowledgeBaseAPI pkb;

    public QueryEvaluatorBase(ProgramKnowledgeBaseAPI pkb) {
        this.pkb = pkb;
    }

    // CALA FUNKCJA I DZIALANIE DO POPRAWY
    // OBECNIE WERSJA PRYMITYWNA, TESTOWA
    // Ewaluuje proste zapytania takie jak: 'stmt s; while w; select w such that Parent(w, s) with w.stmt# = 4'
    @Override
    public List<Pair<ASTNode, Function<ASTNode, String>>> evaluate(QueryTree queryTree) {
        var resultNodes = new ArrayList<ResNode>();
        var node = queryTree.getResultsNode().getFirstChild();

        while(node != null)
        {
            if(node instanceof ResNode)
                resultNodes.add((ResNode) node);

            node = node.getRightSibling();
        }

        var s = resultNodes.get(0).getSynonym();

        var results1 = this.getMatchingNodes(pkb.getAST(), s);

        // CALY KOD DO POPRAWY
        if(queryTree.getSuchThatNode() != null)
        {
            var relNode = queryTree.getSuchThatNode().getFirstChild();
            var relationships = new ArrayList<RelationshipRef>();

            while (relNode != null){
                if(relNode instanceof RelationshipRef)
                    relationships.add((RelationshipRef) relNode);

                relNode = relNode.getRightSibling();
            }

            for (var relRef: relationships) {
                if(relRef.getRelationshipType() == Keyword.PARENT) {
                    var argParent = relRef.getArg(0);
                    var argChild = relRef.getArg(1);

                    var arg1Nodes = getMatchingNodes(pkb.getAST(), argParent.getSynonym());
                    var arg2Nodes = getMatchingNodes(pkb.getAST(), argChild.getSynonym());

                    int argN;
                    if(s == argParent.getSynonym())
                        argN = 0;
                    else
                        argN = 1;

                    results1 = new HashSet<>(getParentChildPairs(arg1Nodes, arg2Nodes)
                            .stream()
                            .map(p -> argN == 0 ? p.getFirst() : p.getSecond())
                            .collect(Collectors.toList()))
                            .stream()
                            .collect(Collectors.toList());
                }
            }
        }

        if(queryTree.getWithNode() != null) {
            var cond = queryTree.getWithNode().getFirstChild();

            return results1.stream()
                    .filter(r -> ((ConditionNode) cond).attrCompare(r))
                    .map(n -> new Pair<>(n, resultNodes.get(0).getExtractor()))
                    .collect(Collectors.toList());
        }

        return results1
                .stream()
                .map(n -> new Pair<>(n, resultNodes.get(0).getExtractor()))
                .collect(Collectors.toList());
    }

    // TEZ DO POPRAWY - POMYŚLEĆ NAD LEPSZĄ METODĄ
    private List<Pair<ASTNode, ASTNode>> getParentChildPairs(List<ASTNode> parentCandidates, List<ASTNode> childCandidates)
    {
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

    private List<ASTNode> getMatchingNodes(ASTNode head, Synonym<?> s) {
        var result = new ArrayList<ASTNode>();
        ASTNode node = head;

        Stack<ASTNode> nodeStack = new Stack<>();
        do {
            if(node == null) {
                if(!nodeStack.empty())
                    node = nodeStack.pop();
                continue;
            }
            nodeStack.add(node.getRightSibling());

            if(s.isDerivative(node))
                result.add(node);

            node = node.getFirstChild();
        } while(!nodeStack.empty() || node != null);

        return result;
    }
}
