package queryprocessor.querytree;

public abstract class Condition extends QTNode
{
    public Condition(String label) {
        super(label);
    }

    abstract boolean attrCompare(Object o);
}
