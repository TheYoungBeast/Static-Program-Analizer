package queryprocessor.querytree;

import pkb.ast.abstraction.ASTNode;
import queryprocessor.preprocessor.Keyword;
import queryprocessor.preprocessor.synonyms.SynonymFactory;

import java.util.function.Function;

public class ResBooleanNode extends ResNode
{
    public ResBooleanNode() {
        super(SynonymFactory.create("", Keyword.BOOLEAN));
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
