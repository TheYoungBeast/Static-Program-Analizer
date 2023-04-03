package frontend.ast;

import frontend.ast.abstraction.ASTNode;
import frontend.ast.abstraction.StatementNode;
import java.util.List;

public class ProcedureNode extends ASTNode {

  String name;

  public List<StatementNode> statements;

  public ProcedureNode(String name, List<StatementNode> statements) {
    this.name = name;
    this.statements = statements;
  }
}
