package cfg;

import pkb.ast.abstraction.ASTNode;
import pkb.ast.abstraction.StatementNode;

public class CfgNode
{
    public CfgNode left = null;
    public CfgNode right = null;
    public ASTNode astNode;

    public CfgNode() {

    }

    public String getText() {
        if(astNode instanceof StatementNode)
            return String.valueOf(((StatementNode) astNode).getStatementId());

        return "CfgNode";
    }
}

