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
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class QueryEvaluatorBase implements QueryEvaluator
{
    public final ProgramKnowledgeBaseAPI pkb;

    public final HashMap<Keyword, BiFunction<
            List<ASTNode>,                  // arg1
            List<ASTNode>,                  // arg2
            List<Pair<ASTNode, ASTNode>>>    // result
            > evalAlgorithms = new HashMap<>();

    public QueryEvaluatorBase(ProgramKnowledgeBaseAPI pkb, EvaluationEngine engine) {
        this.pkb = pkb;

        evalAlgorithms.put(Keyword.PARENT, engine::evaluateParentRel);
        evalAlgorithms.put(Keyword.T_PARENT, engine::evaluateParentTransitiveRel);
        evalAlgorithms.put(Keyword.USES, engine::evaluateUsesRel);
        evalAlgorithms.put(Keyword.MODIFIES, engine::evaluateModifiesRel);
    }

    @Override
    public EvaluationResult evaluate(QueryTree queryTree)
    {
        var resultNodes = new ArrayList<ResNode>();
        var node = queryTree.getResultsNode().getFirstChild();

        /*
         * LinkedHashMap
         * in addition to the uniqueness of elements, the order of elements in which they were added is also guaranteed
         */
        var resultLUT = new LinkedHashMap<Synonym<?>, Set<ASTNode>>();
        var resultExtractors = new LinkedHashMap<Synonym<?>, Function<ASTNode, String>>();

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

        if(queryTree.getWithNode() != null)
        {
            var conditions = new ArrayList<ConditionNode>();
            var condNode =  queryTree.getWithNode().getFirstChild();

            while (condNode != null) {
                if(condNode instanceof ConditionNode)
                    conditions.add((ConditionNode) condNode);

                condNode = condNode.getRightSibling();
            }

            for (var condition: conditions) {
                var wResults = resultLUT.computeIfAbsent(condition.getAttrRef().getSynonym(),
                        l -> getMatchingNodes(pkb.getAST(), condition.getAttrRef().getSynonym()));

                var cResult = wResults.stream()
                        .filter(condition::attrCompare)
                        .collect(Collectors.toList());

                resultLUT.put(condition.getAttrRef().getSynonym(), new HashSet<>(cResult));
            }
        }

        Set<Pair<ASTNode, ASTNode>> pairsInRelationship = new HashSet<>();
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
                for (int i = 0; i < relRef.getArgSize(); i++) {
                    var arg = relRef.getArg(i);
                    resultLUT.computeIfAbsent(arg.getSynonym(), l -> getMatchingNodes(pkb.getAST(), arg.getSynonym()));
                }

                var arg1 = relRef.getArg(0);
                var arg2 = relRef.getArg(1);

                var results = new ArrayList<>(evalAlgorithms.get(relRef.getRelationshipType()).apply(
                        new ArrayList<>(resultLUT.get(arg1.getSynonym())),
                        new ArrayList<>(resultLUT.get(arg2.getSynonym()))
                ));

                var firstSet = new HashSet<ASTNode>();
                var secondSet = new HashSet<ASTNode>();
                int anyMatch = 0;
                for (var resultPair: results) {
                    var firstNode = resultPair.getFirst(); // astnode z relacji
                    var secondNode = resultPair.getSecond(); // astnode z relacji
                    var firstSynonym = arg1.getSynonym();

                    // sprawdz caly LUT czy node z relacji w nim sie zawiera
                    // jesli LUT nie zawiera takiego node'a to nie moze byc poprawnym wynikiem
                    if(resultLUT.get(firstSynonym).stream().anyMatch(x -> x == firstNode)) {
                        var secondSynonym = arg2.getSynonym();

                        if(resultLUT.get(secondSynonym).stream().anyMatch(x -> x == secondNode)) {
                            pairsInRelationship.add(resultPair);
                            firstSet.add(firstNode);
                            secondSet.add(secondNode);
                            anyMatch++;
                        }
                    }
                }

                if(anyMatch == 0) { // no results where matched, from this point no further relationship can hold
                    pairsInRelationship.clear();
                    break;
                }

                resultLUT.put(arg1.getSynonym(), firstSet);
                resultLUT.put(arg2.getSynonym(), secondSet);
            }
        }

        return new EvaluationResult(resultLUT, resultExtractors);
    }

    private Set<ASTNode> getMatchingNodes(ASTNode head, Synonym<?> s) {
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

        return new HashSet<>(result);
    }
}
