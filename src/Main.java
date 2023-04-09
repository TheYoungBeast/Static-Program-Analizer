import pkb.cfg.ControlFlowGraph;
import frontend.lexer.Lexer;
import frontend.lexer.Token;
import frontend.parser.Parser;

import java.util.List;
import java.util.Stack;
import pkb.ProgramKnowledgeBase;
import queryprocessor.evaluator.QueryEvaluator;
import queryprocessor.evaluator.QueryEvaluatorBase;
import queryprocessor.preprocessor.QueryPreprocessorBase;
import queryprocessor.preprocessor.exceptions.InvalidQueryException;
import queryprocessor.preprocessor.exceptions.MissingArgumentException;
import queryprocessor.querytree.QTNode;
import queryprocessor.querytree.QueryTree;
import queryresultprojector.QueryResultProjector;

public class Main {

    public static void main(String[] args)
    {
        Lexer lexer = new Lexer();
        List<Token> tokens = lexer.tokenize("example_source_code.txt");
        ProgramKnowledgeBase pkb = new ProgramKnowledgeBase();
        Parser.parse(tokens, pkb);
        ControlFlowGraph.createCfg(pkb);
        System.out.println(pkb.getAST());

        var qp = new QueryPreprocessorBase();

        QueryTree qt = null;
        try {
            qt = qp.parseQuery("stmt s; while v; Select v such that Parent(v, s) with v.stmt# = 4");
        } catch (InvalidQueryException e) {
            System.err.println(e.explain());
        } catch (MissingArgumentException e) {
            System.err.println(e.explain());
        }

        if(qt == null)
            return;

        var node = qt.getResultsNode();

        // trawersowanie drzewa QTNode / TNode / ASTNode
        Stack<QTNode> nodeStack = new Stack<>();
        do {
            if(node == null) {
                if(!nodeStack.empty())
                    node = nodeStack.pop();
                continue;
            }
            nodeStack.add(node.getRightSibling());
            System.out.println(node.getLabel());
            node = node.getFirstChild();
        } while(!nodeStack.empty() || node != null);

        QueryEvaluator evaluator = new QueryEvaluatorBase(pkb);
        var list = evaluator.evaluate(qt);

        var qrp = new QueryResultProjector();
        qrp.setResultPairs(list);

        System.out.println("\n\nFormatted results:");
        System.out.println(qrp.format());
    }
}
