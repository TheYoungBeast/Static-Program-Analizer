package pkb.ast;

import pkb.ast.abstraction.ContainerNode;
import pkb.ast.abstraction.StatementNode;
import java.util.List;

public class WhileNode extends ContainerNode {

  public WhileNode(int statementId, VariableNode condition, List<StatementNode> statements) {
    super(statementId, condition, statements);
  }
}
