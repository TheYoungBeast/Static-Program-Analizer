package pkb.ast;

import pkb.ast.abstraction.ExpressionNode;

public class ConstantNode extends ExpressionNode {

  int value;

  public ConstantNode(int value) {
    this.value = value;
  }
}
