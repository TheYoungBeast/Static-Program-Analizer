package cfg;

import pkb.ast.IfNode;
import pkb.ast.ProcedureNode;
import pkb.ast.WhileNode;
import pkb.ast.abstraction.ASTNode;
import pkb.ast.abstraction.StatementNode;


public class ControlFlowGraph
{
    public static CfgNode build(ProcedureNode procedureNode) {
        var cfg = new CfgNode();
        cfg.astNode = procedureNode.getFirstChild();
        var last = generateCfg(procedureNode.getFirstChild(), cfg);
        var end = new EndProcedureNode();
        //last.right = end;
        //last.left = end;

        return cfg;
    }

    private static CfgNode generateCfg(ASTNode head, CfgNode cfgHead) {
        CfgNode lastCfgNode = cfgHead;
        var aNode = head;

        while(aNode != null)
        {
            if (aNode instanceof WhileNode) {
                var whileNode = new CfgNode();
                lastCfgNode.left = whileNode;
                whileNode.astNode = aNode;
                var stmtList = aNode.getFirstChild().getRightSibling(); // WhileNode
                                                                                //      |
                                                                                //   condition ——> stmtList
                whileNode.left = new CfgNode();
                lastCfgNode = generateCfg(stmtList.getFirstChild(), whileNode);
                lastCfgNode.left = whileNode;
                whileNode.right = new CfgNode();
                whileNode.right.astNode = aNode.getRightSibling();
                aNode = aNode.getRightSibling();
                lastCfgNode = whileNode.right;
            }
            else if(aNode instanceof IfNode) {
                var then = aNode.getFirstChild().getRightSibling();
                var elsee = then.getRightSibling();

                var ifNode = new CfgNode();
                ifNode.astNode = aNode;
                lastCfgNode.left = ifNode;

                var thenNode = new CfgNode();
                thenNode.astNode = then;
                ifNode.left = thenNode;
                var lastThenStmt = generateCfg(then.getFirstChild(), thenNode);

                var endIfNode = new EndIfNode();
                lastThenStmt.right = endIfNode;

                if(elsee != null) {
                    var elseNode = new CfgNode();
                    elseNode.astNode = elsee;
                    ifNode.right = elseNode;
                    var lastElseStmt = generateCfg(elsee.getFirstChild(), elseNode);
                    lastElseStmt.left = endIfNode;
                }

                lastCfgNode = endIfNode;
            }
            else if(aNode instanceof StatementNode) {
                var cfg = new CfgNode();
                lastCfgNode.left = cfg;
                cfg.astNode = aNode;
                lastCfgNode = cfg;
            }

            if(aNode != null)
                aNode = aNode.getRightSibling();
        }

        return lastCfgNode;
    }
}
