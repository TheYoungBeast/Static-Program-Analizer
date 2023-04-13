package pkb.ast;

import pkb.ast.abstraction.ExpressionNode;
import pkb.ast.abstraction.StatementNode;

public class AssignmentNode extends StatementNode {

  private VariableNode name;

  private ExpressionNode expression;

  public AssignmentNode(int statementId, VariableNode name, ExpressionNode expression) {
    super(statementId);
    this.setName(name);
    this.setExpression(expression);

    // MAGIA HERE
    name.setParent(this);
    expression.setParent(this);
    this.setFirstChild(name);
    name.setRightSibling(expression);
  }

  public VariableNode getName() {
    return name;
  }

  public void setName(VariableNode name) {
    this.name = name;
  }

  public ExpressionNode getExpression() {
    return expression;
  }

  public void setExpression(ExpressionNode expression) {
    this.expression = expression;
  }
}
