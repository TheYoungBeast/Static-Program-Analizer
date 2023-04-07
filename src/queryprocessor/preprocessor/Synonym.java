package queryprocessor.preprocessor;

import pkb.ast.abstraction.ASTNode;

import java.util.Objects;

public class Synonym <T extends ASTNode> {

  private final String identifier;
  private final Keyword keyword;
  private final Class<T> syntaxType;

  public Synonym(String identifier, Keyword keyword, Class<T> syntaxType) {
    this.identifier = identifier;
    this.keyword = keyword;
    this.syntaxType = syntaxType;
  }

  public boolean isDerivative(ASTNode node) {
      return syntaxType.isAssignableFrom(node.getClass());
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

    Synonym synonym = (Synonym) o;
    return identifier.equals(synonym.identifier) && keyword == synonym.keyword;
  }

  @Override
  public int hashCode() {
    return Objects.hash(identifier, keyword);
  }
}
