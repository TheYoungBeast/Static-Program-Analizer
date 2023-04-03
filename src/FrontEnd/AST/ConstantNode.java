package FrontEnd.AST;

public class ConstantNode extends ExpressionNode {

    int value;

    public ConstantNode(int value) {
        this.value = value;
    }
}
