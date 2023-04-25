package cfg;

import pkb.ast.IfNode;
import pkb.ast.ProcedureNode;
import pkb.ast.WhileNode;
import pkb.ast.abstraction.ASTNode;
import pkb.ast.abstraction.StatementNode;


public class ControlFlowGraph
{
    private final CfgNode graph;

    public ControlFlowGraph(CfgNode node) {
        graph = node;
    }

    public boolean pathExists(StatementNode node1, StatementNode node2) {
        return false;
    }

    public static CfgNode build(ProcedureNode procedureNode)
    {
        var cfg = new CfgNode();
        var last = generateCfg(procedureNode.getFirstChild(), cfg);
        var end = new EndProcedureNode();
        //last.right = end;
        //last.left = end;

        return cfg;
    }

    private static CfgNode generateCfg(ASTNode head, CfgNode cfgHead)
    {
        CfgNode lastCfgNode = cfgHead;
        var aNode = head;

        while(aNode != null)
        {
            if (aNode instanceof WhileNode) {
                var whileNode = new CfgNode();
                lastCfgNode.setLeft(whileNode);
                whileNode.setAstNode(aNode);
                var stmtList = aNode.getFirstChild().getRightSibling(); // WhileNode
                                                                                //      |
                                                                                //   condition ——> stmtList
                whileNode.setLeft(new CfgNode());
                lastCfgNode = generateCfg(stmtList.getFirstChild(), whileNode);
                lastCfgNode.setLeft(whileNode);
                whileNode.setRight(new CfgNode());
                whileNode.getRight().setAstNode(aNode.getRightSibling());
                aNode = aNode.getRightSibling();
                lastCfgNode = whileNode.getRight();
            }
            else if(aNode instanceof IfNode) {
                var then = aNode.getFirstChild().getRightSibling(); //   if
                var elsee = then.getRightSibling();             //    /  |  \
                                                                        //  var  then  else
                var ifNode = new CfgNode();
                ifNode.setAstNode(aNode);
                lastCfgNode.setLeft(ifNode);

                var thenNode = new CfgNode();
                thenNode.setAstNode(then.getFirstChild());
                ifNode.setLeft(thenNode);
                var lastThenStmt = generateCfg(then.getFirstChild(), thenNode);

                var endIfNode = new EndIfNode();
                lastThenStmt.setRight(endIfNode);

                if(elsee != null) {
                    var elseNode = new CfgNode();
                    elseNode.setAstNode(elsee.getFirstChild());
                    ifNode.setRight(elseNode);
                    var lastElseStmt = generateCfg(elsee.getFirstChild(), elseNode);
                    lastElseStmt.setLeft(endIfNode);
                }

                lastCfgNode = endIfNode;
            }
            else if(aNode instanceof StatementNode) {
                var cfg = new CfgNode();
                lastCfgNode.setLeft(cfg);
                cfg.setAstNode(aNode);
                lastCfgNode = cfg;
            }

            if(aNode != null)
                aNode = aNode.getRightSibling();
        }

        return lastCfgNode;
    }
}
