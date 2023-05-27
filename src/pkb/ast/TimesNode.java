package pkb.ast;

import pkb.ast.abstraction.ExpressionNode;
import pkb.ast.abstraction.MathExpression;

import java.util.Objects;

public class TimesNode extends MathExpression {

  private ExpressionNode left;

  private ExpressionNode right;

  public TimesNode(ExpressionNode left, ExpressionNode right) {
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
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof TimesNode)) return false;
    TimesNode timesNode = (TimesNode) o;
    return Objects.equals(left, timesNode.left) && Objects.equals(right, timesNode.right);
  }

  @Override
  public int hashCode() {
    return Objects.hash(left, right);
  }
}
