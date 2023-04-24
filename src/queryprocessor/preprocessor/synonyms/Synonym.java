package queryprocessor.preprocessor.synonyms;

import pkb.ast.abstraction.ASTNode;
import queryprocessor.preprocessor.Keyword;

import java.util.Objects;

public class Synonym <T extends ASTNode> {

    protected final String identifier;
    protected final Keyword keyword;
    private final Class<T> syntaxType;

    public Synonym(String identifier, Keyword keyword, Class<T> syntaxType) {
        this.identifier = identifier;
        this.keyword = keyword;
        this.syntaxType = syntaxType;
    }

    public boolean isDerivative(ASTNode node) {
        return syntaxType.isAssignableFrom(node.getClass());
    }

    @SuppressWarnings("unused")
    public boolean isDerivative(Class<T> _class) {
        return syntaxType.isAssignableFrom(_class);
    }

    public String getIdentifier() {
        return identifier;
    }

    public Keyword getKeyword() {
        return keyword;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        Synonym<?> synonym = (Synonym<?>) o;
        return identifier.equals(synonym.identifier) && keyword == synonym.keyword;
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, keyword);
    }
}
