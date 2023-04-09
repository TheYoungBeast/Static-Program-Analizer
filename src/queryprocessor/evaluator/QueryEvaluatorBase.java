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

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class QueryEvaluatorBase implements QueryEvaluator
{
    public final ProgramKnowledgeBaseAPI pkb;

    public QueryEvaluatorBase(ProgramKnowledgeBaseAPI pkb) {
        this.pkb = pkb;
    }

    // METODA DO POPRAWY
    // UPROSCIC PROCES... DUZO ZAGNIEZDZONYCH, SKOMPLIKOWANYCH TYPOW
    // Ewaluuje zapytania zlozone z setow a takze relacji oraz warunkow: 'procedure p; while v; Select p, v such that Parent(p, v) with p.procName = "Rectangle"'
    @Override
    public EvaluationResult evaluate(QueryTree queryTree) {
        var resultNodes = new ArrayList<ResNode>();
        var node = queryTree.getResultsNode().getFirstChild();

        var resultLUT = new HashMap<Synonym<?>, List<ASTNode>>();
        var resultExtractors = new HashMap<Synonym<?>, Function<ASTNode, String>>();

        while(node != null)
        {
            if(node instanceof ResNode)
                resultNodes.add((ResNode) node);

            node = node.getRightSibling();
        }

        for (var resNode: resultNodes) {
            var s = resNode.getSynonym();
            resultLUT.computeIfAbsent(s, l -> this.getMatchingNodes(pkb.getAST(), s));
            resultExtractors.computeIfAbsent(s, e -> resNode.getExtractor());
        }

        if(queryTree.getWithNode() != null) {
            var conditions = new ArrayList<ConditionNode>();
            var condNode =  queryTree.getWithNode().getFirstChild();

            while (condNode != null) {
                if(condNode instanceof ConditionNode)
                    conditions.add((ConditionNode) condNode);

                condNode = condNode.getRightSibling();
            }

            for (var condition: conditions) {
                var wResults = resultLUT.get(condition.getAttrRef().getSynonym());

                var cResult = wResults.stream()
                        .filter(condition::attrCompare)
                        .collect(Collectors.toList());

                resultLUT.put(condition.getAttrRef().getSynonym(), cResult);
            }
        }

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

                    resultLUT.computeIfAbsent(argParent.getSynonym(), l -> getMatchingNodes(pkb.getAST(), argParent.getSynonym()));
                    resultLUT.computeIfAbsent(argChild.getSynonym(), l -> getMatchingNodes(pkb.getAST(), argChild.getSynonym()));

                    var results =
                            new HashSet<>(getParentChildPairs(resultLUT.get(argParent.getSynonym()), resultLUT.get(argChild.getSynonym())))
                            .stream()
                            .collect(Collectors.toList());

                    var parents = results.stream().map(Pair::getFirst).collect(Collectors.toList());
                    var children = results.stream().map(Pair::getSecond).collect(Collectors.toList());

                    resultLUT.put(argParent.getSynonym(), parents);
                    resultLUT.put(argChild.getSynonym(), children);
                }
            }
        }

        return new EvaluationResult(resultLUT, resultExtractors);
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
