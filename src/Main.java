import cfg.CfgNode;
import designextractor.DesignExtractor;
import pkb.ast.IfNode;
import pkb.ast.ProcedureNode;
import pkb.ast.WhileNode;
import pkb.ast.abstraction.ASTNode;
import pkb.ast.abstraction.StatementNode;
import pkb.cfg.ControlFlowGraph;
import frontend.lexer.Lexer;
import frontend.lexer.Token;
import frontend.parser.Parser;

import java.util.*;

import pkb.ProgramKnowledgeBase;
import queryprocessor.evaluator.EvalEngine;
import queryprocessor.evaluator.abstraction.EvaluationEngine;
import queryprocessor.evaluator.abstraction.QueryEvaluator;
import queryprocessor.evaluator.QueryEvaluatorBase;
import queryprocessor.preprocessor.QueryPreprocessorBase;
import queryprocessor.preprocessor.exceptions.InvalidQueryException;
import queryprocessor.preprocessor.exceptions.MissingArgumentException;
import queryprocessor.querytree.QueryTree;
import queryresultprojector.QueryResultProjector;
import utils.Pair;

/**
 * Quality of Service
 * Zamiennik makr - bo biedna java nie ma własnych...
 */

class QoS {
    public static final boolean verbose = false;
    public static final boolean printStackTree = false;
}

public class Main {

    public static void main(String[] args) throws Exception {
        var sourceFile = Arrays.stream(args).findFirst().orElse(null);

        if(sourceFile == null)
            throw new Exception("# No source file provided");

        Lexer lexer = new Lexer();
        List<Token> tokens = lexer.tokenize(sourceFile);
        ProgramKnowledgeBase pkb = new ProgramKnowledgeBase();
        Parser.parse(tokens, pkb);
        DesignExtractor.extract(pkb);
        ControlFlowGraph.createCfg(pkb);
        if(QoS.verbose)
            System.out.println(pkb.getAST());

        var qp = new QueryPreprocessorBase();

        Scanner scanner = new Scanner(System.in);

        System.out.println("Ready");

        var ast = pkb.getAST();

        ASTNode node = ast;
        var controlFlowGraphs = new ArrayList<CfgNode>();
        var currId = 0;
        CfgNode cfgNode = null;

        var procNode = ast.getFirstChild();
        var procedures = new ArrayList<ASTNode>();
        while(procNode != null) {
            if(procNode instanceof ProcedureNode) {
                procedures.add(procNode);
                procNode = procNode.getRightSibling();
            }
        }

        Stack<Pair<CfgNode, ASTNode>> stack = new Stack<>();
        for (var procedure: procedures)
        {
            CfgNode lastCfgNode = null;
            var aNode = procedure.getFirstChild();
            var i = 0;

            while(aNode != null || !stack.isEmpty()) {
                if(aNode == null) {
                    var pair = stack.pop();
                    aNode = pair.getSecond().getRightSibling();
                    lastCfgNode.right = pair.getFirst();
                    pair.getFirst().right = new CfgNode();
                    pair.getFirst().right.astNode = aNode;
                }

                if(i == 0) {
                    var cfg = new CfgNode();
                    cfg.astNode = aNode;
                    controlFlowGraphs.add(cfg);
                    lastCfgNode = cfg;
                }
                else {
                    if (aNode instanceof WhileNode) {
                        var cfg = new CfgNode();
                        lastCfgNode.left = cfg;
                        cfg.astNode = aNode;
                        lastCfgNode = cfg;

                        stack.add(new Pair<>(cfg, aNode));
                        aNode = aNode.getFirstChild();
                    } else if(aNode instanceof StatementNode) {
                        var cfg = new CfgNode();
                        lastCfgNode.left = cfg;
                        cfg.astNode = aNode;
                        lastCfgNode = cfg;
                    }
                }

                i++;
                aNode = aNode.getRightSibling();
            }

            stack.clear();
        }

        var cfg1 = controlFlowGraphs.get(0);

        var cfgStack = new Stack<CfgNode>();
        cfgStack.add(cfg1);
        var count = 0;
        while(!cfgStack.isEmpty()) {
            var cfg = cfgStack.pop();

            if(cfg != null)
                count++;
            else continue;

            if(cfg.left != null)
                cfgStack.add(cfg.left);

            if(cfg.right != null)
                cfgStack.add(cfg.right);
        }

        Stack<ASTNode> nodeStack = new Stack<>();
        do {
            if(node == null) {
                if(!nodeStack.empty()) {
                    node = nodeStack.pop();
                    if(node instanceof StatementNode)
                        System.out.println("w prawo " + String.valueOf(node));
                }
                continue;
            }
            else {
                if(node instanceof StatementNode)
                    System.out.println("w doł" + String.valueOf(node));
            }
            nodeStack.add(node.getRightSibling());

            if(node instanceof ProcedureNode) {
                var cNode = new CfgNode();
                controlFlowGraphs.add(cNode);
                cfgNode = cNode;
            }

            if(node instanceof StatementNode) {
                var newNode = new CfgNode();
                cfgNode.left = newNode;
                newNode.astNode = node;
                cfgNode = newNode;
            }

            if(node instanceof IfNode) {

            }

            node = node.getFirstChild();
        } while(!nodeStack.empty() || node != null);


        while(true) {
            QueryTree qt = null;
            var queryString = new String[2];

            queryString[0] = String.valueOf(scanner.nextLine()); // deklaracje
            queryString[1] = String.valueOf(scanner.nextLine()); // zapytanie

            try {
                qt = qp.parseQuery(queryString[0] + queryString[1]);

                EvaluationEngine ee = new EvalEngine(pkb);
                QueryEvaluator evaluator = new QueryEvaluatorBase(pkb, ee);
                var evaluationResult = evaluator.evaluate(qt);

                var qrp = new QueryResultProjector();
                qrp.setEvaluationResult(evaluationResult);

                System.out.println(qrp.format());
            } catch (InvalidQueryException | MissingArgumentException e) {
                System.err.println("# " + e.explain());
                if (QoS.printStackTree)
                    e.printStackTrace();
            } catch (Exception e) {
                System.err.println("# " + e.getMessage());
                if (QoS.printStackTree)
                    e.printStackTrace();
            }
        }
    }
}
