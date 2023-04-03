package queryprocessor.preprocessor;

public enum Keyword {
  SELECT("select"),
  SUCH_THAT("such that"),
  WITH("with"),
  PATTERN("pattern"),
  AND("and"),
  STATEMENT("stmt"),
  ASSIGN("assign"),
  WHILE("while"),
  FOLLOWS("Follows"),
  MODIFIES("Modifies"),
  T_MODIFIES("Modifies*"),
  T_FOLLOWS("Follows*"),
  PARENT("Parent"),
  T_PARENT("Parent*"),
  SYNONYM("([a-zA-Z]+[0-9]*).+?((?=such)|(?=with)|(?=pattern))"); // ([a-zA-Z]+[0-9])*.?((?=such)|(?=with)|(?=pattern)|(?=,))

  private final String pattern;

  public String getPattern() {
    return this.pattern;
  }

  Keyword(String pattern) {
    this.pattern = pattern;
  }
}
