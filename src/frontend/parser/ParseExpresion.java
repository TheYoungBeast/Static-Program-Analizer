package frontend.parser;

import static frontend.parser.ParseTerm.parseTerm;
import static frontend.parser.Parser.check;
import static frontend.parser.Parser.match;

import frontend.ast.PlusNode;
import frontend.ast.abstraction.ExpressionNode;
import frontend.lexer.TokenType;

class ParseExpresion {

  static ExpressionNode parseExpression() {
    ExpressionNode left = parseTerm();
    while (check(TokenType.PLUS)) {
      match(TokenType.PLUS);
      ExpressionNode right = parseTerm();
      left = new PlusNode(left, right);
    }
    return left;
  }
}
