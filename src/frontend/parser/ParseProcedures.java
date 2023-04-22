package frontend.parser;

import static frontend.parser.ParseProcedure.parseProcedure;
import static frontend.parser.Parser.isLastToken;

import java.util.ArrayList;
import java.util.List;
import pkb.ast.ProcedureNode;

class ParseProcedures {

  static List<ProcedureNode> parseProcedures() {
    List<ProcedureNode> procedures = new ArrayList<>();
    while (!isLastToken()) {
      ProcedureNode procedure = parseProcedure();
      procedures.add(procedure);
    }
    return procedures;
  }

}
