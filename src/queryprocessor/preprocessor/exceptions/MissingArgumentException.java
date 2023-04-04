package queryprocessor.preprocessor.exceptions;

public class MissingArgumentException extends Exception implements QueryException {

  private static final String defaultMsg = "Missing argument";

  private final String rel;
  private final int l;
  private final String query;

  public MissingArgumentException(String relationship, int line, String query1) {
    super(defaultMsg);
    this.query = query1;
    this.rel = relationship;
    this.l = line;
  }

  @Override
  public String explain() {
    return String.format("%s in relationship %s at line %d: %s", this.getMessage(), rel, l, query);
  }
}
