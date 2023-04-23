package pkb.ast;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import pkb.ast.abstraction.ASTNode;
import pkb.ast.abstraction.StatementNode;
import java.util.List;

public class ProcedureNode extends ASTNode {

  private final String name;

  public final List<StatementNode> statements;

  public ProcedureNode(String name, List<StatementNode> statements) {
    this.name = name;
    this.statements = statements;

    ASTNode last = null;
    this.setFirstChild(statements.get(0));
    for (var s: statements) {
      s.setParent(this);

      if(last!= null)
        last.setRightSibling(s);

      last = s;
    }
  }

  public String getName() {
    return name;
  }
}
