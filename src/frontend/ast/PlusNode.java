package frontend.ast;

import frontend.ast.abstraction.ExpressionNode;

public class PlusNode extends ExpressionNode {

  private ExpressionNode left;

  private ExpressionNode right;

  public PlusNode(ExpressionNode left, ExpressionNode right) {
    this.setLeft(left);
    this.setRight(right);
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
}
