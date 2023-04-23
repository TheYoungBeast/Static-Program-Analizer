package pkb.ast;

import pkb.ast.abstraction.StatementNode;

public class CallNode extends StatementNode {

  private final String calledProcedureName;

  public CallNode(int statementId, String calledProcedureName) {
    super(statementId);
    this.calledProcedureName = calledProcedureName;
  }

  public String getCalledProcedureName() {
    return calledProcedureName;
  }
}
