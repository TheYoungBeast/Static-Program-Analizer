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

                var results1 = results.stream().map(Pair::getFirst).collect(Collectors.toList());
                var results2 = results.stream().map(Pair::getSecond).collect(Collectors.toList());

                resultLUT.put(arg1.getSynonym(), new HashSet<>(results1));
                resultLUT.put(arg2.getSynonym(), new HashSet<>(results2));
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
