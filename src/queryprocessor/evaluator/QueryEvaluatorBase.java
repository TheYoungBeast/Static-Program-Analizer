package queryprocessor.evaluator;

import pkb.ast.abstraction.ASTNode;
import pkb.ProgramKnowledgeBaseAPI;
import queryprocessor.preprocessor.Synonym;
import queryprocessor.querytree.ConditionNode;
import queryprocessor.querytree.QueryTree;
import queryprocessor.querytree.ResNode;
import utils.Pair;

import java.util.ArrayList;
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

    @Override
    public List<Pair<ASTNode, Function<ASTNode, String>>> evaluate(QueryTree queryTree) {
        var resultNodes = new ArrayList<ResNode>();
        var node = queryTree.getResultsNode().getFirstChild();

        while(node != null)
        {
            if(node instanceof ResNode)
                resultNodes.add((ResNode) node);
            resultNodes.add((ResNode) node);
            node = node.getRightSibling();
        }

        var s = resultNodes.get(0).getSynonym();

        var results1 = this.getMatchingNodes(pkb.getAST(), s);

        if(queryTree.getWithNode() != null) {
            var cond = queryTree.getWithNode().getFirstChild();

            return results1.stream()
                    .filter(r -> ((ConditionNode) cond).attrCompare(r))
                    .map(n -> new Pair<ASTNode, Function<ASTNode, String>>(n, resultNodes.get(0).getExtractor()))
                    .collect(Collectors.toList());
        }

        return this.getMatchingNodes(pkb.getAST(), s)
                .stream()
                .map(n -> new Pair<ASTNode, Function<ASTNode, String>>(n, resultNodes.get(0).getExtractor()))
                .collect(Collectors.toList());
    }

    private List<ASTNode> getMatchingNodes(ASTNode head, Synonym s) {
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
