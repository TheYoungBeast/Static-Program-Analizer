package queryprocessor.preprocessor.synonyms;

import pkb.ast.ProcedureNode;
import pkb.ast.abstraction.ASTNode;
import queryprocessor.preprocessor.Keyword;

public class NamedProcedureSynonym extends NamedSynonym<ProcedureNode>
{
    public NamedProcedureSynonym(String identifier) {
        super(identifier, Keyword.PROCEDURE, ProcedureNode.class);
    }

    @Override
    public boolean isDerivative(ASTNode node) {
        return super.isDerivative(node) && ((ProcedureNode) node).getName().equals(identifier);
    }

    @Override
    public boolean isDerivative(Class<ProcedureNode> _class) {
        throw new UnsupportedOperationException();
    }
}
