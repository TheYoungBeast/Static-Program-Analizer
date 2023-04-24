package queryprocessor.preprocessor;

import pkb.ast.abstraction.ASTNode;
import pkb.ast.abstraction.StatementNode;

public class StatementIdSynonym extends Synonym<StatementNode> {

    public StatementIdSynonym(String identifier) {
        super(identifier, Keyword.STATEMENT, StatementNode.class);
    }

    @Override
    public boolean isDerivative(ASTNode node) {
        return super.isDerivative(node) && ((StatementNode) node).getStatementId() == Integer.parseInt(identifier);
    }

    @Override
    public boolean isDerivative(Class<StatementNode> _class) {
        throw new UnsupportedOperationException();
    }
}
