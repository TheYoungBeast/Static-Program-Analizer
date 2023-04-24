package frontend.parser;

import static frontend.parser.ParseStatements.parseStatements;
import static frontend.parser.Parser.getProcedureFromPkbOrNew;
import static frontend.parser.Parser.match;

import pkb.ast.ProcedureNode;
import pkb.ast.abstraction.StatementNode;
import frontend.lexer.TokenType;
import java.util.List;

class ParseProcedure {

  static ProcedureNode parseProcedure() {
    match(TokenType.PROCEDURE);
    String name = match(TokenType.NAME).getValue();
    match(TokenType.LBRACE);
    List<StatementNode> statements = parseStatements();
    match(TokenType.RBRACE);

    return getProcedureFromPkbOrNew(name).setStatements(statements);
  }
}
