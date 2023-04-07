package queryprocessor.evaluator;

import frontend.ast.AssignmentNode;
import frontend.ast.WhileNode;
import frontend.ast.abstraction.ASTNode;
import frontend.ast.abstraction.StatementNode;
import frontend.ast.abstraction.TNode;
import frontend.lexer.TokenType;
import pkb.ProgramKnowledgeBaseAPI;
import queryprocessor.preprocessor.Keyword;
import queryprocessor.preprocessor.Synonym;
import queryprocessor.querytree.QTNode;
import queryprocessor.querytree.QueryTree;
import queryprocessor.querytree.ResNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class QueryEvaluatorBase implements QueryEvaluator
{
    public final ProgramKnowledgeBaseAPI pkb;

    public QueryEvaluatorBase(ProgramKnowledgeBaseAPI pkb) {
        this.pkb = pkb;
    }

    @Override
    public List<ASTNode> evaluate(QueryTree queryTree) {
        var synonyms = new ArrayList<Synonym>();
        var node = queryTree.getResultNode().getFirstChild();

        while(node != null)
        {
            synonyms.add(((ResNode) node).getSynonym());
            node = node.getRightSibling();
        }

        var s = synonyms.get(0);

        return this.getMatchingNodes(pkb.getAST(), s);
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
