package frontend.lexer;

public enum TokenType {
  PROCEDURE("procedure"),
  WHILE("while"),
  LBRACE("\\{"),
  RBRACE("\\}"),
  LPAREN("\\("),
  RPAREN("\\)"),
  EQUALS("="),
  TIMES("\\*"),
  PLUS("\\+"),
  MINUS("-"),
  SEMICOLON(";"),
  NAME("[a-zA-Z][a-zA-Z\\d#]*"),
  CONSTANT("\\d+"),
  WHITESPACE("\\s+");

  public final String pattern;

  TokenType(String pattern) {
    this.pattern = pattern;
  }
}
