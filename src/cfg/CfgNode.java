package cfg;

import pkb.ast.abstraction.ASTNode;

public class CfgNode
{
    public CfgNode left = null;
    public CfgNode right = null;
    public CfgNode parent = null;
    public int StmtId;
    public ASTNode astNode;

    public CfgNode() {

    }
}
