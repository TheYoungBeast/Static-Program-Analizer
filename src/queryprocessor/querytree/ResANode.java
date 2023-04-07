package queryprocessor.querytree;

import pkb.ast.ProcedureNode;
import pkb.ast.VariableNode;
import pkb.ast.abstraction.ASTNode;

import java.util.function.Function;

public class ResANode extends ResNode {
    private final AttrRef attrRef;

    public ResANode(AttrRef ref) {
        super(ref.getSynonym());
        this.attrRef = ref;
    }

    @Override
    public Function<ASTNode, String> getExtractor() {
        return (ASTNode node) -> {
            switch (attrRef.getAttr()) {
                case procName:
                    return ((ProcedureNode) node).getName();
                case varName:
                    return ((VariableNode) node).getName();
                default: // case s.stmt#
                    return super.getExtractor().apply(node);
            }
        };
    }
}
