package queryprocessor.querytree;

import pkb.ast.ConstantNode;
import pkb.ast.ProcedureNode;
import pkb.ast.VariableNode;
import pkb.ast.abstraction.ASTNode;
import pkb.ast.abstraction.StatementNode;
import queryprocessor.preprocessor.synonyms.Synonym;

public class AttrRef extends QTNode
{
    private final Synonym<?> synonym;
    private final AttrName attrName;

    public AttrRef(Synonym<?> s, AttrName a) {
        super("AttrRef: " + s.getIdentifier() + "." + a.getName());
        this.synonym = s;
        this.attrName = a;
    }

    public Synonym<?> getSynonym() {
        return synonym;
    }

    public AttrName getAttr() {
        return attrName;
    }

    public Object extractAttributeValue(ASTNode o) {
        switch (attrName)
        {
            case value:
                if(o instanceof ConstantNode)
                    return ((ConstantNode) o).getValue();
            case varName:
                if(o instanceof VariableNode)
                    return ((VariableNode) o).getName();
            case stmtNo:
                if(o instanceof StatementNode)
                    return ((StatementNode) o).getStatementId();
            case procName:
                if(o instanceof ProcedureNode)
                    return ((ProcedureNode) o).getName();
        }

        throw new UnsupportedOperationException();
    }
}
