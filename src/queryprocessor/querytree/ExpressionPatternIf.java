package queryprocessor.querytree;

import pkb.ast.abstraction.ASTNode;

import java.util.Set;

public class ExpressionPatternIf extends ExpressionPattern
{
    private final ExpressionPattern thenExp;
    private final ExpressionPattern elseExp;
    public ExpressionPatternIf(ExpressionPattern thenExp, ExpressionPattern elseExp) {
        super(thenExp);
        this.elseExp = elseExp;
        this.thenExp = thenExp;
    }

    @Override
    public boolean matchesPattern(ASTNode node, Set<ASTNode> allowedVariables) {
        if(!thenExp.getSynonym().isDerivative(node))
            return false;

        var variable = node.getFirstChild();

        if(!isVariableAllowed(variable, allowedVariables) || !matchesVariable(variable) )
            return false;

        var rightSibling = variable.getRightSibling();
        return thenExp.compare(rightSibling) && rightSibling.getRightSibling() != null && elseExp.compare(rightSibling.getRightSibling());
    }
}
