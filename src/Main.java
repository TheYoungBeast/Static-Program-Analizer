import cfg.CfgNode;
import designextractor.DesignExtractor;
import pkb.ast.ProcedureNode;
import pkb.ast.abstraction.ASTNode;
import frontend.lexer.Lexer;
import frontend.lexer.Token;
import frontend.parser.Parser;

import java.util.*;

import pkb.ProgramKnowledgeBase;
import pkb.cfg.ControlFlowGraph;
import queryprocessor.evaluator.EvalEngine;
import queryprocessor.evaluator.abstraction.EvaluationEngine;
import queryprocessor.evaluator.abstraction.QueryEvaluator;
import queryprocessor.evaluator.QueryEvaluatorBase;
import queryprocessor.preprocessor.QueryPreprocessorBase;
import queryprocessor.preprocessor.exceptions.InvalidQueryException;
import queryprocessor.preprocessor.exceptions.MissingArgumentException;
import queryprocessor.querytree.QueryTree;
import queryresultprojector.QueryResultProjector;

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

        var procNode = ast.getFirstChild();
        var procedures = new ArrayList<ASTNode>();
        while(procNode != null) {
            if(procNode instanceof ProcedureNode) {
                procedures.add(procNode);
                procNode = procNode.getRightSibling();
            }
        }


        for (var procedure: procedures)
        {
           controlFlowGraphs.add(cfg.ControlFlowGraph.build((ProcedureNode)procedure));
        }

        var cfg1 = controlFlowGraphs.get(0);


        Class breakpoint = null;

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
