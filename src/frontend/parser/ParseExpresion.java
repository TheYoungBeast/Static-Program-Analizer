package frontend.parser;

import static frontend.parser.ParseTerm.parseTerm;
import static frontend.parser.Parser.check;
import static frontend.parser.Parser.match;

import frontend.lexer.TokenType;
import pkb.ast.MinusNode;
import pkb.ast.PlusNode;
import pkb.ast.abstraction.ExpressionNode;

class ParseExpresion {

  static ExpressionNode parseExpression() {
    ExpressionNode left = parseTerm();
    while (check(TokenType.PLUS) || check(TokenType.MINUS)) {
      if (check(TokenType.PLUS)) {
        match(TokenType.PLUS);
        ExpressionNode right = parseTerm();
        left = new PlusNode(left, right);
      } else {
        match(TokenType.MINUS);
        ExpressionNode right = parseTerm();
        left = new MinusNode(left, right);
      }
    }
    return left;
  }
}
