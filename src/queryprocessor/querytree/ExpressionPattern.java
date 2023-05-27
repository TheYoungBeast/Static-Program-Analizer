package queryprocessor.querytree;

import pkb.ast.*;
import pkb.ast.abstraction.ASTNode;
import pkb.ast.abstraction.MathExpression;
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

    protected boolean compare(ASTNode variableNode)
    {
        if(subExpressionTree.isEmpty())
            return true;

        var searchExpressionTree = subExpressionTree.get();

        var steps = 0;
        var stmtExpressionTree = variableNode.getRightSibling();
        while(stmtExpressionTree != null)
        {
            if(!lookBehind && steps > 1)
                return false;

            var depth = new Depth();

            if(compareTrees(searchExpressionTree, stmtExpressionTree, depth, lookAhead)) {
                if(lookAhead)
                    return true;

                // the special case when comparing fragment tree to bigger tree e.g. x*y+z+v with x*y+z
                // Matches only if lookAhead is allowed.
                // pattern a("x", "x*y+z"_)
                if(stmtExpressionTree.getRightSibling() == null)
                    return true;
            }

            stmtExpressionTree = stmtExpressionTree.getRightSibling();
            steps++;
        }

        return false;
    }

    private class Depth {
        public Integer level = 0;
    }

    private boolean compareTrees(ASTNode searchExp, ASTNode stmtExp, Depth depth, final boolean lookAhead)
    {
        /*1. both empty */
        if (searchExp == null && stmtExp == null)
            return true;
        else if (lookAhead && searchExp == null)
            return true;

        /* 2. both non-empty -> compare them */
        if (searchExp != null && stmtExp != null) {
            depth.level++;

            var isSearchExpMathExpression = searchExp instanceof MathExpression;
            var isStmtExpMathExpression = stmtExp instanceof MathExpression;

            if(isSearchExpMathExpression && isStmtExpMathExpression)
                return (searchExp.getClass().equals(stmtExp.getClass())
                        && compareTrees(searchExp.getFirstChild(), stmtExp.getFirstChild(), depth, lookAhead)
                        && compareTrees(searchExp.getRightSibling(), stmtExp.getRightSibling(), depth, lookAhead));
            else if (isStmtExpMathExpression)
                return compareTrees(searchExp, stmtExp.getFirstChild(), depth, lookAhead)
                        || compareTrees(searchExp, stmtExp.getFirstChild().getRightSibling(), depth, lookAhead);
            else return (searchExp.equals(stmtExp)
                        && compareTrees(searchExp.getFirstChild(), stmtExp.getFirstChild(), depth, lookAhead)
                        && compareTrees(searchExp.getRightSibling(), stmtExp.getRightSibling(), depth, lookAhead));
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
