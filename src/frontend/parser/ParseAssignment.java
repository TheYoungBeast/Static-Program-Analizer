package frontend.parser;

import static frontend.parser.ParseExpresion.parseExpression;
import static frontend.parser.Parser.match;

import pkb.ast.AssignmentNode;
import pkb.ast.VariableNode;
import pkb.ast.abstraction.ExpressionNode;
import pkb.ast.abstraction.StatementNode;
import frontend.lexer.TokenType;

class ParseAssignment {

  static StatementNode parseAssignment(int id) {
    VariableNode name = new VariableNode(match(TokenType.NAME).getValue());
    match(TokenType.EQUALS);
    ExpressionNode expression = parseExpression();
    match(TokenType.SEMICOLON);
    return new AssignmentNode(id, name, expression);
  }
}
