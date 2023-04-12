import pkb.ast.WhileNode;
import pkb.ast.abstraction.ASTNode;
import pkb.ast.abstraction.TNode;
import pkb.cfg.ControlFlowGraph;
import frontend.lexer.Lexer;
import frontend.lexer.Token;
import frontend.parser.Parser;

import java.util.List;
import java.util.Stack;
import pkb.ProgramKnowledgeBase;
import queryprocessor.evaluator.EvalEngine;
import queryprocessor.evaluator.QueryEvaluator;
import queryprocessor.evaluator.QueryEvaluatorBase;
import queryprocessor.preprocessor.QueryPreprocessorBase;
import queryprocessor.preprocessor.exceptions.InvalidQueryException;
import queryprocessor.preprocessor.exceptions.MissingArgumentException;
import queryprocessor.querytree.QTNode;
import queryprocessor.querytree.QueryTree;
import queryresultprojector.QueryResultProjector;

/**
 * Quality of Service
 * Zamiennik makr - bo biedna java nie ma własnych...
 */

class QoS {
    public static boolean verbose = false;
    public static boolean printStackTree = true;
}

public class Main {

    public static void main(String[] args)
    {
        Lexer lexer = new Lexer();
        List<Token> tokens = lexer.tokenize("example_source_code.txt");
        ProgramKnowledgeBase pkb = new ProgramKnowledgeBase();
        Parser.parse(tokens, pkb);
        ControlFlowGraph.createCfg(pkb);
        if(QoS.verbose)
            System.out.println(pkb.getAST());

        var qp = new QueryPreprocessorBase();

        QueryTree qt = null;
        try {
            qt = qp.parseQuery("while w; stmt s;Select <w, s> such that Parent(w, s);");
        } catch (InvalidQueryException | MissingArgumentException e) {
            System.err.println(e.explain());
            if(QoS.printStackTree)
                e.printStackTrace();
        }

        if(qt == null)
            return;

        TNode node = qt.getResultsNode();

        if(QoS.verbose) {
            // trawersowanie drzewa QTNode / TNode / ASTNode
            Stack<TNode> nodeStack = new Stack<>();
            do {
                if (node == null) {
                    if (!nodeStack.empty())
                        node = nodeStack.pop();
                    continue;
                }
                nodeStack.add(node.getRightSibling());
                System.out.println(((QTNode)node).getLabel());
                node = node.getFirstChild();
            } while (!nodeStack.empty() || node != null);
        }

        node = pkb.getAST();
        Stack<TNode> nodeStack = new Stack<>();
        do {
            if (node == null) {
                if (!nodeStack.empty())
                    node = nodeStack.pop();
                continue;
            }
            nodeStack.add(node.getRightSibling());
            if(node instanceof WhileNode)
                System.out.println(((WhileNode)node).getStatementId());
            node = node.getFirstChild();
        } while (!nodeStack.empty() || node != null);

        var ee = new EvalEngine();
        QueryEvaluator evaluator = new QueryEvaluatorBase(pkb, ee);
        var evaluationResult = evaluator.evaluate(qt);

        var qrp = new QueryResultProjector();
        qrp.setEvaluationResult(evaluationResult);

        System.out.println("\n");
        System.out.println(qrp.format());
    }
}
