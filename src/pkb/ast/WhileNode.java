package pkb.ast;

import java.util.List;
import pkb.ast.abstraction.ContainerNode;
import pkb.ast.abstraction.StatementNode;

public class WhileNode extends ContainerNode {

  public WhileNode(int statementId, VariableNode condition, List<StatementNode> statements) {
    super(statementId, condition, statements);
  }
}
