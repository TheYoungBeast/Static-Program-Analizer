package frontend.parser;

import static frontend.parser.ParseStatements.parseStatements;
import static frontend.parser.ParseTerm.parseTerm;
import static frontend.parser.Parser.match;

import frontend.ast.VariableNode;
import frontend.ast.WhileNode;
import frontend.ast.abstraction.StatementNode;
import frontend.lexer.TokenType;
import java.util.List;

class ParseWhile {

  static WhileNode parseWhile(int id) {
    match(TokenType.WHILE);
    VariableNode condition = (VariableNode) parseTerm();
    match(TokenType.LBRACE);
    List<StatementNode> statements = parseStatements();
    match(TokenType.RBRACE);
    WhileNode whileNode = new WhileNode(id, condition, statements);
    for (StatementNode statement : whileNode.statements) {
      statement.setParent(whileNode);
    }
    return whileNode;
  }
}
