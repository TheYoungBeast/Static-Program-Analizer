package frontend.ast;

import frontend.ast.abstraction.ExpressionNode;

public class ConstantNode extends ExpressionNode {

  int value;

  public ConstantNode(int value) {
    this.value = value;
  }
}
