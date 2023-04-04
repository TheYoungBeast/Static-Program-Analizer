package queryprocessor.querytree;

public class ConditionNode extends QTNode
{
    private final String synonym;
    private final String attr;
    private final String condValue;

    public ConditionNode(String synonym, String attr, String condValue) {
        super("Attr: " + synonym+"."+attr + " = " + condValue);
        this.synonym = synonym;
        this.attr = attr;
        this.condValue = condValue;
    }
}
