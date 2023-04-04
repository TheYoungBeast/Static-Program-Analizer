package pkb;

import cfg.CFGNode;
import frontend.ast.ProcedureNode;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class ProgramKnowledgeBase implements ProgramKnowledgeBaseAPI {

  private final Map<Integer, Set<String>> modifies;

  private final Map<Integer, Set<String>> uses;

  private ProcedureNode ast;

  private CFGNode cfg;

  public ProgramKnowledgeBase() {
    modifies = new HashMap<>();
    uses = new HashMap<>();
  }

  public void addAST(ProcedureNode node) {
    ast = node;
  }

  public void addCFG(CFGNode node) {
    cfg = node;
  }

  public ProcedureNode getAST() {
    return ast;
  }

  public CFGNode getCFG() {
    return cfg;
  }

  public void addModifies(int s, String v) {
    modifies.computeIfAbsent(s, k -> new LinkedHashSet<>()).add(v);
  }

  public void addModifies(int s, Set<String> vs) {
    modifies.computeIfAbsent(s, k -> new LinkedHashSet<>()).addAll(vs);
  }

  public Set<String> getModifies(int s) {
    return modifies.getOrDefault(s, Collections.emptySet());
  }

  public void addUses(int s, String v) {
    uses.computeIfAbsent(s, k -> new LinkedHashSet<>()).add(v);
  }

  public void addUses(int s, Set<String> vs) {
    uses.computeIfAbsent(s, k -> new LinkedHashSet<>()).addAll(vs);
  }

  public Set<String> getUses(int s) {
    return uses.getOrDefault(s, Collections.emptySet());
  }
}
