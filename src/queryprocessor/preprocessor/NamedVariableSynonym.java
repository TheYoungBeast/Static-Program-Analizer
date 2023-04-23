package queryprocessor.preprocessor;

import pkb.ast.VariableNode;
import pkb.ast.abstraction.ASTNode;

public class NamedVariableSynonym extends Synonym<VariableNode>
{
    public NamedVariableSynonym(String identifier) {
        super(identifier, Keyword.VARIABLE, VariableNode.class);
    }

    @Override
    public boolean isDerivative(ASTNode node) {
        return super.isDerivative(node) && ((VariableNode) node).getName().equals(this.identifier);
    }

    @Override
    public boolean isDerivative(Class<VariableNode> _class) {
        throw new UnsupportedOperationException();
    }
}
