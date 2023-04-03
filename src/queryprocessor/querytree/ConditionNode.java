package queryprocessor.querytree;

public class ConditionNode extends QTNode
{
    private String synonym;
    private String attr;

    public ConditionNode(String synonym, String attr) {
        super("Attr");
        this.synonym = synonym;
        this.attr = attr;
    }
}
