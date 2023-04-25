package cfg;

import pkb.ast.abstraction.ASTNode;
import pkb.ast.abstraction.StatementNode;

public class CfgNode
{
    private CfgNode left = null;
    private CfgNode right = null;
    private StatementNode astNode;

    public CfgNode getLeft() {
        return left;
    }

    public void setLeft(CfgNode left) {
        this.left = left;
    }

    public CfgNode getRight() {
        return right;
    }

    public void setRight(CfgNode right) {
        this.right = right;
    }

    public StatementNode getAstNode() {
        return astNode;
    }

    public void setAstNode(ASTNode astNode) {
        this.astNode = (StatementNode) astNode;
    }

    public int getStmtId() {
        if(astNode == null)
            return -1;

        return astNode.getStatementId();
    }
}

