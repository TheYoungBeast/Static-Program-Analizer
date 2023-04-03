package FrontEnd.AST;

import java.util.List;

public class ProcedureNode extends ASTNode {

    String name;

    List<StatementNode> statements;

    public ProcedureNode(String name, List<StatementNode> statements) {
        this.name = name;
        this.statements = statements;
    }
}
