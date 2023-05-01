package queryprocessor.preprocessor.synonyms;

import pkb.ast.abstraction.ASTNode;
import queryprocessor.preprocessor.Keyword;

import java.util.UUID;

public class UnconstrainedSynonym<T extends ASTNode> extends Synonym<T>
{
    public UnconstrainedSynonym(Keyword keyword, Class<T> syntaxType) {
        super(String.valueOf(UUID.randomUUID()), keyword, syntaxType);
    }
}
