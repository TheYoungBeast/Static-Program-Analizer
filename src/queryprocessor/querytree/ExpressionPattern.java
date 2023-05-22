package queryprocessor.querytree;

import pkb.ast.AssignmentNode;
import pkb.ast.IfNode;
import pkb.ast.WhileNode;
import pkb.ast.abstraction.ASTNode;
import queryprocessor.preprocessor.synonyms.Synonym;

import java.util.Optional;
import java.util.Set;

public class ExpressionPattern extends QTNode
{
    private final Synonym<?> synonym;
    private final Optional<ASTNode> subExpressionTree;
    private final ArgNode leftHandExpression;
    private final boolean lookBehind;
    private final boolean lookAhead;

    public ExpressionPattern(Synonym<?> synonym, Synonym<?> leftHand, ASTNode subExpressionTree, boolean lookAhead, boolean lookBehind)
    {
        super("");
        this.synonym = synonym;
        this.subExpressionTree = Optional.ofNullable(subExpressionTree);
        this.leftHandExpression = new ArgNode(leftHand, 1);
        this.lookBehind = lookBehind;
        this.lookAhead = lookAhead;
    }

    public ExpressionPattern(ExpressionPattern pattern) {
        super("");
        this.subExpressionTree = pattern.subExpressionTree;
        this.leftHandExpression = pattern.leftHandExpression;
        this.synonym = pattern.synonym;
        this.lookBehind = pattern.lookBehind;
        this.lookAhead = pattern.lookAhead;
    }

    public Synonym<?> getSynonym() {
        return synonym;
    }

    public ArgNode getLeftHandExpression() {
        return leftHandExpression;
    }

    public boolean matchesPattern(ASTNode node, Set<ASTNode> allowedVariables) {
        return matchesPattern(node, node.getFirstChild(), allowedVariables);
    }

    protected boolean matchesPattern(ASTNode node, ASTNode variable, Set<ASTNode> allowedVariables)
    {
        if(!synonym.isDerivative(node))
            return false;

        if(!isVariableAllowed(variable, allowedVariables) || !matchesVariable(variable) )
            return false;

        if(node instanceof IfNode)
            throw new UnsupportedOperationException();

        if(node instanceof WhileNode){
            // While node cannot have a body
            // Placeholder fits anything e.g. while(_,_),
            return subExpressionTree.isEmpty(); // empty exp tree means the pattern is correct since those nodes cannot have a body
        }

        if(node instanceof AssignmentNode)
            return compare(node.getFirstChild());

        return false;
    }

    protected boolean compare(ASTNode child)
    {
        if(subExpressionTree.isEmpty())
            return true;

        var steps = 0;
        var expressionNode = child.getRightSibling();
        while(expressionNode != null)
        {
            if(!lookBehind && steps > 1)
                return false;

            var depth = new Depth();
            if(compareTrees(subExpressionTree.get(), expressionNode, depth, lookAhead)) {
                if(lookAhead)
                    return true;

                var lastNode = expressionNode;
                var i = depth.level;
                while(i > 0) {
                    i--;
                    //lastNode = lastNode.getRightSibling();
                }

                // the special case when comparing fragment tree to bigger tree e.g. x*y+z+v with x*y+z
                // Matches only if lookAhead is allowed.
                // pattern a("x", "x*y+z"_)
                if(lastNode.getRightSibling() == null)
                    return true;
            }

            expressionNode = expressionNode.getRightSibling();
            steps++;
        }

        return false;
    }

    private class Depth {
        public Integer level = 0;
    }

    private boolean compareTrees(ASTNode a, ASTNode b, Depth depth, final boolean lookAhead)
    {
        /*1. both empty */
        if (a == null && b == null)
            return true;
        else if (lookAhead && a == null)
            return true;

        /* 2. both non-empty -> compare them */
        if (a != null && b != null) {
            depth.level++;
            return (a.equals(b)
                    && compareTrees(a.getFirstChild(), b.getFirstChild(), depth, lookAhead)
                    && compareTrees(a.getRightSibling(), b.getRightSibling(), depth, lookAhead));
        }

        /* 3. one empty, one not -> false */
        return false;
    }

    protected boolean isVariableAllowed(ASTNode variable, Set<ASTNode> allowedVariables) {
        if(variable == null)
            return false;
        return allowedVariables.contains(variable);
    }

    protected boolean matchesVariable(ASTNode variable) {
        if(variable == null)
            return false;

        return leftHandExpression.getSynonym().isDerivative(variable);
    }
}
