package queryprocessor.preprocessor.synonyms;

import pkb.ast.abstraction.ASTNode;
import pkb.ast.abstraction.StatementNode;
import queryprocessor.preprocessor.Keyword;
import queryprocessor.preprocessor.synonyms.Synonym;

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
