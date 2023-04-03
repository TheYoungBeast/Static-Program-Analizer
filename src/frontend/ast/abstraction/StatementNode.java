package frontend.ast.abstraction;

public abstract class StatementNode extends ASTNode {

  private int statementId;

  protected StatementNode(int statementId) {
    this.setStatementId(statementId);
  }

  public int getStatementId() {
    return statementId;
  }

  public void setStatementId(int statementId) {
    this.statementId = statementId;
  }
}
