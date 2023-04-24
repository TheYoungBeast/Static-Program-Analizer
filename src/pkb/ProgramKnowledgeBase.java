package pkb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import pkb.ast.ConstantNode;
import pkb.ast.ProcedureNode;
import pkb.ast.ProgramNode;
import pkb.ast.VariableNode;
import pkb.ast.abstraction.ASTNode;
import pkb.cfg.CFGNode;

public class ProgramKnowledgeBase implements ProgramKnowledgeBaseAPI {

  private final Map<ASTNode, Set<VariableNode>> modifies;

  private final Map<ASTNode, Set<VariableNode>> uses;

  private final Map<ASTNode, Set<ProcedureNode>> calls;

  private final Set<VariableNode> varTable;

  private final Set<ConstantNode> constTable;

  private final Set<ProcedureNode> procTable;

  private ProgramNode ast;

  private List<CFGNode> cfg;

  public ProgramKnowledgeBase() {
    modifies = new ConcurrentHashMap<>();
    uses = new ConcurrentHashMap<>();
    calls = new HashMap<>();
    varTable = new LinkedHashSet<>();
    constTable = new LinkedHashSet<>();
    procTable = new LinkedHashSet<>();
    cfg = new ArrayList<>();
  }

  public void addAST(ProgramNode node) {
    ast = node;
  }

  public Map<ASTNode, Set<VariableNode>> getModifies() {
    return modifies;
  }

  public Map<ASTNode, Set<VariableNode>> getUses() {
    return uses;
  }

  public Map<ASTNode, Set<ProcedureNode>> getCalls() {
    return calls;
  }

  public void addCFG(CFGNode node) {
    cfg.add(node);
  }

  @Override
  public ProgramNode getAST() {
    return ast;
  }

  @Override
  public List<CFGNode> getCFG() {
    return cfg;
  }

  public void addModifies(ASTNode s, VariableNode v) {
    modifies.computeIfAbsent(s, k -> new LinkedHashSet<>()).add(v);
  }

  public void addModifies(ASTNode s, Set<VariableNode> vs) {
    modifies.computeIfAbsent(s, k -> new LinkedHashSet<>()).addAll(vs);
  }

  @Override
  public Set<VariableNode> getModifies(ASTNode s) {
    return modifies.getOrDefault(s, Collections.emptySet());
  }

  public void addUses(ASTNode s, VariableNode v) {
    uses.computeIfAbsent(s, k -> new LinkedHashSet<>()).add(v);
  }

  public void addUses(ASTNode s, Set<VariableNode> vs) {
    uses.computeIfAbsent(s, k -> new LinkedHashSet<>()).addAll(vs);
  }

  @Override
  public Set<VariableNode> getUses(ASTNode s) {
    return uses.getOrDefault(s, Collections.emptySet());
  }

  @Override
  public Set<ProcedureNode> getCalls(ASTNode p) {
    return calls.getOrDefault(p, Collections.emptySet());
  }

  @Override
  public Set<VariableNode> getVarTable() {
    return varTable;
  }

  @Override
  public Set<ConstantNode> getConstTable() {
    return constTable;
  }

  public Set<ProcedureNode> getProcTable() {
    return procTable;
  }

  public void addVariableToVarTable(VariableNode v) {
    varTable.add(v);
  }

  public void addConstantToConstTable(ConstantNode v) {
    constTable.add(v);
  }

  public void addProcedureToProcTable(ProcedureNode p) {
    procTable.add(p);
  }

}