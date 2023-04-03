package PKB;

import FrontEnd.ASTNode;
import FrontEnd.CFG;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

// Program Knowledge Base
public class PKB implements PKBAPI {

  private final Map<Integer, Set<Integer>> follows;

  private final Map<Integer, Set<Integer>> followsStar;

  private final Map<Integer, Set<Integer>> parent;

  private final Map<Integer, Set<Integer>> parentStar;

  private final Map<Integer, Set<String>> modifies;

  private final Map<Integer, Set<String>> uses;

  private ASTNode ast;

  private final CFG cfg;

  public PKB() {
    follows = new HashMap<>();
    followsStar = new HashMap<>();
    parent = new LinkedHashMap<>();
    parentStar = new LinkedHashMap<>();
    modifies = new HashMap<>();
    uses = new HashMap<>();
    cfg = new CFG();
  }

  public void addCFGNode(int statementId) {
    cfg.addNode(statementId);
  }

  public void addCFGEdge(int fromStatementId, int toStatementId) {
    cfg.addEdge(fromStatementId, toStatementId);
  }

  public void addFollows(int s1, int s2) {
    follows.computeIfAbsent(s1, k -> new HashSet<>()).add(s2);
  }

  private void addFollowsStars(int s1, Set<Integer> followsSet) {
    followsStar.put(s1, followsSet);
  }

  public void addParent(int s1, int s2) {
    parent.computeIfAbsent(s1, k -> new LinkedHashSet<>()).add(s2);
  }

  public void addModifies(int s, String v) {
    modifies.computeIfAbsent(s, k -> new LinkedHashSet<>()).add(v);
  }

  private void addParentStars(int s1, Set<Integer> followsSet) {
    parentStar.put(s1, followsSet);
  }

  public void addUses(int s, String v) {
    uses.computeIfAbsent(s, k -> new LinkedHashSet<>()).add(v);
  }

  public void addAST(ASTNode node) {
    ast = node;
  }

  public void computeTransitiveClosures() {
    computeTransitiveClosure(follows, this::addFollowsStars);
    computeTransitiveClosure(parent, this::addParentStars);
  }

  private void computeTransitiveClosure(Map<Integer, Set<Integer>> directRelation,
      BiConsumer<Integer, Set<Integer>> addTransitiveClosure) {
    for (Integer stmt1 : directRelation.keySet()) {
      Set<Integer> transitiveClosure = new HashSet<>();
      computeTransitiveClosure(stmt1, directRelation, transitiveClosure);
      addTransitiveClosure.accept(stmt1, transitiveClosure);
    }
  }

  private void computeTransitiveClosure(
      int currentStmt,
      Map<Integer, Set<Integer>> directRelation,
      Set<Integer> transitiveClosure
  ) {
    Set<Integer> direct = directRelation.get(currentStmt);
    if (direct != null) {
      for (Integer nextStmt : direct) {
        if (transitiveClosure.add(nextStmt)) {
          computeTransitiveClosure(nextStmt, directRelation, transitiveClosure);
        }
      }
    }
  }
}
