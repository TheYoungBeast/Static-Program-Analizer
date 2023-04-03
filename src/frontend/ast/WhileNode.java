package frontend.ast;

import frontend.ast.abstraction.ContainerNode;
import frontend.ast.abstraction.StatementNode;
import java.util.List;

public class WhileNode extends ContainerNode {

  public WhileNode(int statementId, VariableNode condition, List<StatementNode> statements) {
    super(statementId, condition, statements);
  }
}
