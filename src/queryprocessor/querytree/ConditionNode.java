package queryprocessor.querytree;

import pkb.ast.ProcedureNode;
import pkb.ast.abstraction.StatementNode;

public class ConditionNode extends QTNode
{
    private final String condValue;
    private final AttrRef attrRef;

    public ConditionNode(AttrRef ref, String condValue) {
        super("Attr: " + ref.getSynonym().getIdentifier()+"."+ref.getAttr().getName() + " = " + condValue);
        this.attrRef = ref;

        ref.setParent(this);
        this.setFirstChild(ref);
        this.condValue = condValue;
    }

    public boolean attrCompare(Object o)
    {
        switch (getAttrRef().getAttr())
        {
            case stmtNo:
                if(o instanceof StatementNode)
                    return ((StatementNode) o).getStatementId() == Integer.parseInt(condValue);
            case procName:
                if(o instanceof ProcedureNode)
                    return ((ProcedureNode) o).getName().equals(condValue);
        }

        return false;
    }

    public AttrRef getAttrRef() {
        return attrRef;
    }
}
