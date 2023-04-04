package frontend.ast;

import frontend.ast.abstraction.ASTNode;
import frontend.ast.abstraction.ContainerNode;
import frontend.ast.abstraction.StatementNode;
import java.util.List;

public class WhileNode extends ContainerNode {

  public WhileNode(int statementId, VariableNode condition, List<StatementNode> statements) {
    super(statementId, condition, statements);

    this.setFirstChild(statements.get(0));

    ASTNode last = null;
    for (var s: statements) {
      //s.setParent(this);

      if(last!= null)
        last.setRightSibling(s);

      last = s;
    }
  }
}
