package frontend.parser;

import static frontend.parser.Parser.check;
import static frontend.parser.Parser.match;

import frontend.lexer.TokenType;
import pkb.ast.TimesNode;
import pkb.ast.abstraction.ExpressionNode;

class ParseTerm {

  static ExpressionNode parseTerm() {
    ExpressionNode left = ParseFactor.parseFactor();
    while (check(TokenType.TIMES)) {
      match(TokenType.TIMES);
      ExpressionNode right = ParseFactor.parseFactor();
      left = new TimesNode(left, right);
    }
    return left;
  }
}