package queryprocessor.querytree;

import frontend.ast.abstraction.ASTNode;

import java.util.function.Function;

public class ResBooleanNode extends ResNode
{
    public ResBooleanNode() {
        super(null);
    }

    @Override
    public Function<ASTNode, String> getExtractor() {
        return (ASTNode node) -> {
            if(node != null)
                return String.valueOf(true);

            return String.valueOf(false);
        };
    }
}
