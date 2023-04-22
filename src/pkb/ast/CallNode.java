package pkb.ast;

import pkb.ast.abstraction.StatementNode;

public class CallNode extends StatementNode {

  private final String calledProcedure;

  public CallNode(int statementId, String calledProcedure) {
    super(statementId);
    this.calledProcedure = calledProcedure;
  }
}
