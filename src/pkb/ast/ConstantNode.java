package pkb.ast;

import java.util.Objects;
import pkb.ast.abstraction.ExpressionNode;

public class ConstantNode extends ExpressionNode {

  private final int value;

  public ConstantNode(int value) {
    this.value = value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConstantNode that = (ConstantNode) o;
    return value == that.value;
  }

  public int getValue() {
    return value;
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }
}
