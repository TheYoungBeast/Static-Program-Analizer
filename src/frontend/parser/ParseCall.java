package frontend.parser;

import static frontend.parser.Parser.getProcedureFromPkbOrNew;
import static frontend.parser.Parser.match;

import frontend.lexer.TokenType;
import pkb.ast.CallNode;
import pkb.ast.abstraction.StatementNode;

class ParseCall {

  static StatementNode parseCall(int id) {
    match(TokenType.CALL);
    String calledProcedureName = match(TokenType.NAME).getValue();
    match(TokenType.SEMICOLON);
    return new CallNode(id, getProcedureFromPkbOrNew(calledProcedureName));
  }
}
