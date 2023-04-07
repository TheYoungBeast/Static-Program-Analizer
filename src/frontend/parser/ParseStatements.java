package frontend.parser;

import static frontend.parser.ParseStatement.parseStatement;
import static frontend.parser.Parser.check;

import pkb.ast.abstraction.StatementNode;
import frontend.lexer.TokenType;
import java.util.ArrayList;
import java.util.List;

class ParseStatements {

  static List<StatementNode> parseStatements() {
    List<StatementNode> statements = new ArrayList<>();
    while (!check(TokenType.RBRACE)) {
      StatementNode statement = parseStatement();
      statements.add(statement);
    }
    return statements;
  }
}
