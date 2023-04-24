package pkb.ast;

import pkb.ast.abstraction.StatementNode;

public class CallNode extends StatementNode {

  private final ProcedureNode calledProcedure;

  public CallNode(int statementId, ProcedureNode calledProcedure) {
    super(statementId);
    this.calledProcedure = calledProcedure;
  }

  public ProcedureNode getCalledProcedure() {
    return calledProcedure;
  }
}
