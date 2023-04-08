package queryprocessor.querytree;

public enum AttrName
{
    procName("procName"),
    varName("varName"),
    value("value"),
    stmtNo("stmt#");

    private final String attrName;

    AttrName(String attr) {
        this.attrName = attr;
    }

    public String getName() {
        return attrName;
    }
}
