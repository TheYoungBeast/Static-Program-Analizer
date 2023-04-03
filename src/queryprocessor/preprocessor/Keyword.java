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
  SYNONYM("(?<!(\\(\\s*))(([a-zA-Z]+[0-9]*)\\s?((?=such)|(?=with)|(?=pattern)|(?=,)|(?=$)))(?!\\))"), // match synonyms between select ... such/with/pattern
  ATTR_COND("([a-zA-Z]+[0-9]*\\.(procName|varName|stmt#)).*?(?=\\=)"), // match synonyms with attributes
  PROCNAME("procName"),
  VARNAME("varName"),
  STMTNUMBER("stmt#");

  private final String pattern;

  public String getPattern() {
    return this.pattern;
  }

  Keyword(String pattern) {
    this.pattern = pattern;
  }
}
