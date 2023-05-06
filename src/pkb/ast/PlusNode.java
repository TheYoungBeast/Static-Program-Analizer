package pkb.ast;

import pkb.ast.abstraction.ExpressionNode;

public class PlusNode extends ExpressionNode {

  private ExpressionNode left;

  private ExpressionNode right;

  public PlusNode(ExpressionNode left, ExpressionNode right) {
    this.setLeft(left);
    this.setRight(right);

    left.setParent(this);
    this.setFirstChild(left);
    left.setRightSibling(right);
    right.setParent(this);
  }

  public ExpressionNode getLeft() {
    return left;
  }

  public void setLeft(ExpressionNode left) {
    this.left = left;
  }

  public ExpressionNode getRight() {
    return right;
  }

  public void setRight(ExpressionNode right) {
    this.right = right;
  }

  @Override
  public boolean equals(Object obj) {
    if(this == obj)
      return true;

    if(obj.getClass() != this.getClass())
      return false;

    return this.getLeft().equals(((PlusNode) obj).getLeft())
            && this.getRight().equals(((PlusNode) obj).getRight());
  }
}
