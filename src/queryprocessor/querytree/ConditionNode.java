package queryprocessor.querytree;

import pkb.ast.AssignmentNode;
import pkb.ast.abstraction.StatementNode;

public class ConditionNode extends QTNode
{
    private final String condValue;
    public final AttrRef attrRef;

    public ConditionNode(AttrRef ref, String condValue) {
        super("Attr: " + ref.getSynonym().getIdentifier()+"."+ref.getAttr().getName() + " = " + condValue);
        this.attrRef = ref;

        ref.setParent(this);
        this.setFirstChild(ref);
        this.condValue = condValue;
    }

    public boolean attrCompare(Object o)
    {
        switch (attrRef.getAttr())
        {
            case stmtNo:
                if(o instanceof StatementNode)
                    return ((StatementNode) o).getStatementId() == Integer.parseInt(condValue);
        }

        return false;
    }
}
