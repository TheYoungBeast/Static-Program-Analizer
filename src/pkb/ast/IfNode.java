package pkb.ast;

import java.util.List;
import pkb.ast.abstraction.ContainerNode;
import pkb.ast.abstraction.StatementNode;

public class IfNode extends ContainerNode {

  public final List<StatementNode> elseStatements;

  public IfNode(int statementId, VariableNode condition, List<StatementNode> thenStatements, List<StatementNode> elseStatements) {
    super(statementId, condition, thenStatements);
    this.elseStatements = elseStatements;
    setParentAndSibling(elseStatements);

    // strona 16 Handbook, temporal fix
    this.setFirstChild(condition);
    condition.setRightSibling(thenStatements.get(0));
    thenStatements.get(thenStatements.size()-1).setRightSibling(elseStatements.get(0));
  }
}
