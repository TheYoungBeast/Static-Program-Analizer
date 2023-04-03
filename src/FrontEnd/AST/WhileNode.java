package FrontEnd.AST;

import java.util.List;

public class WhileNode extends StatementNode {

    private VariableNode condition;

    private List<StatementNode> statements;

    public WhileNode(int statementId, VariableNode condition, List<StatementNode> statements) {
        super(statementId);
        this.setCondition(condition);
        this.setStatements(statements);
    }

    public VariableNode getCondition() {
        return condition;
    }

    public void setCondition(VariableNode condition) {
        this.condition = condition;
    }

    public List<StatementNode> getStatements() {
        return statements;
    }

    public void setStatements(List<StatementNode> statements) {
        this.statements = statements;
    }
}
