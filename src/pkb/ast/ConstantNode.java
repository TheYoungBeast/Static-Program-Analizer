package pkb.ast;

import pkb.ast.abstraction.ExpressionNode;

public class ConstantNode extends ExpressionNode {

  final int value;

  public ConstantNode(int value) {
    this.value = value;
  }
}
