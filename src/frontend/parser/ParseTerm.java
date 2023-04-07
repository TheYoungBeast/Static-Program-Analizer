package frontend.parser;

import static frontend.parser.Parser.check;
import static frontend.parser.Parser.match;

import pkb.ast.ConstantNode;
import pkb.ast.VariableNode;
import pkb.ast.abstraction.ExpressionNode;
import frontend.lexer.TokenType;

class ParseTerm {

  static ExpressionNode parseTerm() {
    if (check(TokenType.CONSTANT)) {
      return new ConstantNode(Integer.parseInt(match(TokenType.CONSTANT).getValue()));
    } else {
      return new VariableNode(match(TokenType.NAME).getValue());
    }
  }
}
