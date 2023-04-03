package QueryProcessor.QueryTree;

public class ArgNode extends QTNode
{
    private int ArgNum = 0;

    public ArgNode(String label, int ArgNum) {
        super("arg"+ArgNum+label);
        this.ArgNum = ArgNum;
    }

    public int getArgNum() {
        return ArgNum;
    }
}
