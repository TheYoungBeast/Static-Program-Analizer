package queryprocessor.preprocessor;

import java.util.Objects;

public class Synonym {

  private final String identifier;

  private final Keyword keyword;

  public Synonym(String identifier, Keyword keyword) {
    this.identifier = identifier;
    this.keyword = keyword;
  }

  public String getIdentifier() {
    return identifier;
  }

  public Keyword getKeyword() {
    return keyword;
  }

  @Override
  public boolean equals(Object o) {
      if (this == o) {
          return true;
      }
      if (o == null || getClass() != o.getClass()) {
          return false;
      }
    Synonym synonym = (Synonym) o;
    return identifier.equals(synonym.identifier) && keyword == synonym.keyword;
  }

  @Override
  public int hashCode() {
    return Objects.hash(identifier, keyword);
  }
}
