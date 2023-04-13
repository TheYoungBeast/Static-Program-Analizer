package pkb.ast;

import java.util.Objects;
import pkb.ast.abstraction.ExpressionNode;

public class VariableNode extends ExpressionNode {

  private String name;

  public VariableNode(String name) {
    this.setName(name);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    VariableNode that = (VariableNode) o;
    return Objects.equals(name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }
}
