package queryprocessor.preprocessor.synonyms;

import pkb.ast.*;
import pkb.ast.abstraction.ASTNode;
import pkb.ast.abstraction.StatementNode;
import queryprocessor.preprocessor.Keyword;

public class SynonymFactory
{
    public static Synonym<? extends ASTNode> create(String identifier, Keyword k)
    {
        Class<? extends ASTNode> t;
        switch (k) {
            case STATEMENT:
               t = StatementNode.class;
                break;
            case ASSIGN:
                t = AssignmentNode.class;
                break;
            case WHILE:
                t = WhileNode.class;
                break;
            case PROCEDURE:
                t = ProcedureNode.class;
                break;
            case VARIABLE:
                t = VariableNode.class;
                break;
            case CONSTANT:
                t = ConstantNode.class;
                break;
            case BOOLEAN:
                t = ASTNode.class;
                identifier = "";
                break;
            case IF:
                t = IfNode.class;
                break;
            case CALL:
                t = CallNode.class;
                break;
            case PROG_LINE:
                t = StatementNode.class;
                break;
            default:
                throw new UnsupportedOperationException("Keyword support not implemented");
        }

        if(identifier.trim().equals(Keyword.PLACEHOLDER.getRegExpr()))
            return new UnconstrainedSynonym(k, t);

        return new Synonym(identifier, k, t);
    }
}
