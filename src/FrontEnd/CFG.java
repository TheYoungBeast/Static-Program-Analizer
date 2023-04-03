package FrontEnd;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CFG {
  private Map<Integer, Set<Integer>> graph;

  public CFG() {
    graph = new HashMap<>();
  }

  public void addNode(int statementId) {
    graph.putIfAbsent(statementId, new HashSet<>());
  }

  public void addEdge(int fromStatementId, int toStatementId) {
    addNode(fromStatementId);
    addNode(toStatementId);
    graph.get(fromStatementId).add(toStatementId);
  }

  public Set<Integer> getSuccessors(int statementId) {
    return graph.get(statementId);
  }
}
