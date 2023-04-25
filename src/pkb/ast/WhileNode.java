package pkb.ast;

import pkb.ast.abstraction.ContainerNode;
import pkb.ast.abstraction.StatementNode;
import java.util.List;

public class WhileNode extends ContainerNode {

  private final StmtList stmtList;

  public WhileNode(int statementId, VariableNode condition, List<StatementNode> statements) {
    super(statementId, condition, statements);
    this.stmtList = new StmtList(this, statements);
    this.setFirstChild(condition);
    condition.setParent(this);
    condition.setRightSibling(stmtList);
  }
}
