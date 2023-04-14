package frontend.parser;

import static frontend.parser.ParseFactor.parseFactor;
import static frontend.parser.ParseStatements.parseStatements;
import static frontend.parser.Parser.match;

import frontend.lexer.TokenType;
import java.util.List;
import pkb.ast.IfNode;
import pkb.ast.VariableNode;
import pkb.ast.abstraction.StatementNode;

class ParseIf {

  static StatementNode parseIf(int id) {
    match(TokenType.IF);
    VariableNode condition = (VariableNode) parseFactor();
    match(TokenType.THEN);
    match(TokenType.LBRACE);
    List<StatementNode> thenStatements = parseStatements();
    match(TokenType.RBRACE);
    match(TokenType.ELSE);
    match(TokenType.LBRACE);
    List<StatementNode> elseStatements = parseStatements();
    match(TokenType.RBRACE);
    return new IfNode(id, condition, thenStatements, elseStatements);
  }

}
