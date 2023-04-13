package frontend.parser;

import static frontend.parser.ParseExpresion.parseExpression;
import static frontend.parser.Parser.check;
import static frontend.parser.Parser.match;

import pkb.ast.ConstantNode;
import pkb.ast.VariableNode;
import pkb.ast.abstraction.ExpressionNode;
import frontend.lexer.TokenType;

class ParseFactor {

  static ExpressionNode parseFactor() {
    if (check(TokenType.CONSTANT)) {
      return new ConstantNode(Integer.parseInt(match(TokenType.CONSTANT).getValue()));
    } else if (check(TokenType.NAME)) {
      return new VariableNode(match(TokenType.NAME).getValue());
    } else {
      match(TokenType.LPAREN);
      ExpressionNode expression = parseExpression();
      match(TokenType.RPAREN);
      return expression;
    }
  }
}
