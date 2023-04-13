package pkb.ast;

import pkb.ast.abstraction.ExpressionNode;

public class TimesNode extends ExpressionNode {

  private ExpressionNode left;

  private ExpressionNode right;

  public TimesNode(ExpressionNode left, ExpressionNode right) {
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
