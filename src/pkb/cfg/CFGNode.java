package pkb.cfg;

import java.util.ArrayList;
import java.util.List;

public class CFGNode {

  int statementId;

  List<CFGNode> successors;

  CFGNode(int statementId) {
    this.statementId = statementId;
    this.successors = new ArrayList<>();
  }

  void addSuccessor(CFGNode successor) {
    this.successors.add(successor);
  }
}
