package queryprocessor.querytree;

import pkb.ast.ProcedureNode;
import pkb.ast.abstraction.StatementNode;

public class ConditionNode extends QTNode
{
    private final AttrRef attrRef;
    private final AttrValue attrValue;

    public ConditionNode(AttrRef ref, AttrValue attrValue) {
        super("Attr: " + ref.getSynonym().getIdentifier()+"."+ref.getAttr().getName() + " = " + attrValue.getValue());
        this.attrRef = ref;

        ref.setParent(this);
        ref.setRightSibling(attrValue);
        attrValue.setParent(this);
        this.setFirstChild(ref);
        this.attrValue = attrValue;
    }

    public boolean attrCompare(Object o)
    {
        switch (getAttrRef().getAttr())
        {
            case stmtNo:
                if(o instanceof StatementNode)
                    return ((StatementNode) o).getStatementId() == Integer.parseInt(attrValue.getValue());
            case procName:
                if(o instanceof ProcedureNode)
                    return ((ProcedureNode) o).getName().equals(attrValue.getValue());
        }

        return false;
    }

    public AttrRef getAttrRef() {
        return attrRef;
    }
}
