package queryprocessor.preprocessor.synonyms;

import pkb.ast.abstraction.ASTNode;
import queryprocessor.preprocessor.Keyword;

public class NamedSynonym<T extends ASTNode> extends Synonym<T>
{
    public NamedSynonym(String identifier, Keyword keyword, Class<T> syntaxType) {
        super(identifier, keyword, syntaxType);
    }
}
