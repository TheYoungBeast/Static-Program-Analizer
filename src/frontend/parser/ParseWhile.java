package frontend.parser;

import static frontend.parser.ParseStatements.parseStatements;
import static frontend.parser.ParseFactor.parseFactor;
import static frontend.parser.Parser.match;

import pkb.ast.VariableNode;
import pkb.ast.WhileNode;
import pkb.ast.abstraction.StatementNode;
import frontend.lexer.TokenType;
import java.util.List;

class ParseWhile {

  static WhileNode parseWhile(int id) {
    match(TokenType.WHILE);
    VariableNode condition = (VariableNode) parseFactor();
    match(TokenType.LBRACE);
    List<StatementNode> statements = parseStatements();
    match(TokenType.RBRACE);
    return new WhileNode(id, condition, statements);
  }
}
