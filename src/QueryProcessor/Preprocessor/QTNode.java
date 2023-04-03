package QueryProcessor.Preprocessor;

import FrontEnd.AST.ASTNode;
import FrontEnd.AST.TNode;

public class QTNode implements TNode
{
    protected final String label;
    protected QTNode firstChild = null;
    protected QTNode rightSibling = null;
    protected QTNode up = null;

    public QTNode(String label) {
        this.label = label;

    }

    public String getLabel() {
        return label;
    }

    @Override
    public QTNode getFirstChild() {
        return firstChild;
    }

    @Override
    public QTNode getRightSibling() {
        return rightSibling;
    }

    @Override
    public QTNode getUp() {
        return up;
    }

    @Override
    public void SetFirstChild(TNode node) {
        firstChild = (QTNode) node;
    }

    @Override
    public void SetRightSibling(TNode node) {
        rightSibling = (QTNode) node;
    }

    @Override
    public void setUp(TNode node) {
        up = (QTNode) node;
    }
}

class ResultNode extends QTNode
{
    public ResultNode() {
        super("Result");
    }
}

class SuchThatNode extends QTNode
{
    public SuchThatNode(String label) {
        super(label);
    }
}

class ResNode extends QTNode
{
    public ResNode(String label) {
        super(label);
    }
}
