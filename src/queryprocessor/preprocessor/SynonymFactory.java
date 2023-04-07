package queryprocessor.preprocessor;

import frontend.ast.AssignmentNode;
import frontend.ast.WhileNode;
import frontend.ast.abstraction.StatementNode;

public class SynonymFactory {
    public static Synonym create(String identifier, Keyword k) {
        Class<?> t;
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
            default:
                throw new UnsupportedOperationException("Keyword support not implemented");
        }

        return new Synonym(identifier, k, t);
    }
}
