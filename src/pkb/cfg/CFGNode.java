package pkb.cfg;

import java.util.ArrayList;
import java.util.List;

public class CFGNode {

  final int statementId;

  final List<CFGNode> successors;

  CFGNode(int statementId) {
    this.statementId = statementId;
    this.successors = new ArrayList<>();
  }

  void addSuccessor(CFGNode successor) {
    this.successors.add(successor);
  }
}
