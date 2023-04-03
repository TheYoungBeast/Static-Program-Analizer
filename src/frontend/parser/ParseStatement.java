package frontend.parser;

import static frontend.parser.ParseAssignment.parseAssignment;
import static frontend.parser.ParseWhile.parseWhile;
import static frontend.parser.Parser.check;
import static frontend.parser.Parser.updateRelations;

import frontend.ast.abstraction.StatementNode;
import frontend.lexer.TokenType;
import java.util.concurrent.atomic.AtomicInteger;

class ParseStatement {

  private static final AtomicInteger statementIdGenerator = new AtomicInteger(0);

  static StatementNode parseStatement() {
    int id = statementIdGenerator.incrementAndGet();
    StatementNode statement;
    if (check(TokenType.WHILE)) {
      statement = parseWhile(id);
    } else {
      statement = parseAssignment(id);
    }
    updateRelations(statement);
    return statement;
  }
}
