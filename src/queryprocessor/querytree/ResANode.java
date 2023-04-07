package queryprocessor.querytree;

import frontend.ast.ProcedureNode;
import frontend.ast.VariableNode;
import frontend.ast.abstraction.ASTNode;
import queryprocessor.preprocessor.Synonym;

import java.util.function.Function;

import static queryprocessor.querytree.AttrName.*;

public class ResANode extends ResNode {
    private final AttrRef attrRef;

    public ResANode(AttrRef ref) {
        super(ref.getSynonym());
        this.attrRef = ref;
    }

    @Override
    public Function<ASTNode, String> getExtractor() {
        return (ASTNode node) -> {
            switch (attrRef.getAttrName()) {
                case procName:
                    return ((ProcedureNode) node).getName();
                case varName:
                    return ((VariableNode) node).getName();
                default:
                    return super.getExtractor().apply(node);
            }
        };
    }
}
