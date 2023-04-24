package queryprocessor.querytree;

public abstract class ConditionNode extends QTNode
{
    public ConditionNode(String label) {
        super(label);
    }

    abstract boolean attrCompare(Object o);
}
