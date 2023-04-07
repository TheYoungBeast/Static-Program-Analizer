package frontend.parser;

import static frontend.parser.ParseExpresion.parseExpression;
import static frontend.parser.Parser.match;

import pkb.ast.AssignmentNode;
import pkb.ast.abstraction.ExpressionNode;
import pkb.ast.abstraction.StatementNode;
import frontend.lexer.TokenType;

class ParseAssignment {

  static StatementNode parseAssignment(int id) {
    String name = match(TokenType.NAME).getValue();
    match(TokenType.EQUALS);
    ExpressionNode expression = parseExpression();
    match(TokenType.SEMICOLON);
    return new AssignmentNode(id, name, expression);
  }
}
