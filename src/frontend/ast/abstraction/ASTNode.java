package frontend.ast.abstraction;

// rozdział 16.3 Handbook
public abstract class ASTNode implements TNode {

  private ASTNode firstchild;

  private ASTNode rightSibling;

  private ASTNode parent;

  public ASTNode() {
    firstchild = null;
    rightSibling = null;
    parent = null;
  }

  @Override
  public ASTNode getFirstChild() {
    return firstchild;
  }

  @Override
  public ASTNode getRightSibling() {
    return rightSibling;
  }

  @Override
  public ASTNode getParent() {
    return parent;
  }

  @Override
  public void SetFirstChild(TNode node) {
    firstchild = (ASTNode) node;
  }

  @Override
  public void SetRightSibling(TNode node) {
    rightSibling = (ASTNode) node;
  }

  @Override
  public void setParent(TNode node) {
    parent = (ASTNode) node;
  }
}

