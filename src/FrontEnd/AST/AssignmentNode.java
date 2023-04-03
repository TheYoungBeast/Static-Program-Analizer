package FrontEnd.AST;

public class AssignmentNode extends StatementNode {

    private String name;

    private ExpressionNode expression;

    public AssignmentNode(int statementId, String name, ExpressionNode expression) {
        super(statementId);
        this.setName(name);
        this.setExpression(expression);
    }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

    public ExpressionNode getExpression() {
        return expression;
    }

    public void setExpression(ExpressionNode expression) {
        this.expression = expression;
    }
}
