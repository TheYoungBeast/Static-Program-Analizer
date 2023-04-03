package frontend.ast.abstraction;

import frontend.ast.VariableNode;
import java.util.List;

public abstract class ContainerNode extends StatementNode {

  public VariableNode condition;

  public List<StatementNode> statements;

  protected ContainerNode(int statementId, VariableNode condition, List<StatementNode> statements) {
    super(statementId);
    this.condition = condition;
    this.statements = statements;
  }
}
