package frontend.parser;

import static frontend.parser.ParseProcedures.parseProcedures;

import java.util.List;
import pkb.ast.ProcedureNode;
import pkb.ast.ProgramNode;

class ParseProgram {

  static ProgramNode parseProgram() {
    List<ProcedureNode> procedures = parseProcedures();

    return new ProgramNode(procedures);
  }
}
