package queryprocessor.preprocessor;

public enum Keyword {
  SELECT("Select","select"),
  SUCH_THAT("Such That","such that"),
  WITH("With","with"),
  WITH_CLAUSE("With-cl", "with\\s+[a-zA-Z0-9]+\\.[a-zA-Z#]+\\s*=\\s*(([0-9]+)|(\\s*\\\"(.*?)\\\"))"),
  PATTERN("Pattern","pattern"),
  BOOLEAN("Boolean", "BOOLEAN"),
  IF("If", "if"),
  AND("And","and"),
  CONSTANT("Constant", "constant"),
  PLACEHOLDER("Placeholder", "_"),
  STATEMENT("Statement","stmt"),
  CONSTANT("Constant", "constant"),
  ASSIGN("Assign","assign"),
  WHILE("While","while"),
  PROCEDURE("Procedure","procedure"),
  VARIABLE("Variable","variable"),
  FOLLOWS("Follows","Follows\\s*(?=\\()"),
  T_FOLLOWS("Follows*","Follows\\*\\s*(?=\\()"),
  MODIFIES("Modifies","Modifies\\s*\\(.*?\\)"),
  T_MODIFIES("Modifies*","Modifies\\*\\s*\\(.*?\\)"),
  CALLS("Calls","Calls\\s*\\(.*?\\)"),
  T_CALLS("Calls*","Calls\\*\\s*\\(.*?\\)"),
  USES("Uses","Uses\\s*\\(.*?\\)"),
  T_USES("Uses*","Uses\\*\\s*\\(.*?\\)"),
  AFFECTS("Affects", "Affects\\s*\\(.*?\\)"),
  T_AFFECTS("Affects*", "Affects\\*\\s*\\(.*?\\)"),
  PARENT("Parent","Parent\\s*\\(.*?\\)"),
  T_PARENT("Parent*","Parent\\*\\s*\\(.*?\\)"),
  RESULT_TUPLE("\\s*\\<(.*?)\\>\\s*"),
  SYNONYM("[a-zA-Z]+[0-9]*"),
  SYNONYMS("(?<=select)\\s*(.*?)((?=such)|(?=with)|(?=pattern)|(?=$))"), // match synonyms between select ... such/with/pattern/$
  ATTR_COND("([a-zA-Z]+[0-9]*\\.(procName|varName|stmt#)).*?(?=\\=)"), // match synonyms with attributes
  ATTR2("(?<=\\=)\\s*([0-9]+)|((?<=\\\")[a-zA-Z_]+)(?=\\\")"), // extract what's after attribute | [with p1.stmt#=9]
  ARGS("(?<=\\()\\s*[a-zA-Z_]+[0-9]*\\s*(,\\s*[a-zA-Z]+[0-9]*)*\\s*(?=\\))"), // extract args from func
  PROCNAME("procName"),
  VARNAME("varName"),
  VALUE("value"),
  STMTNUMBER("stmt#");

  private final String regExpr;
  private final String name;

  public String getRegExpr() {
    return this.regExpr;
  }
  public String getName() {
    return this.name;
  }

  Keyword(String name, String regExpr) {
    this.regExpr = regExpr;
    this.name = name;
  }

  Keyword(String regExpr) {
    this.regExpr = regExpr;
    this.name = "";
  }
}
