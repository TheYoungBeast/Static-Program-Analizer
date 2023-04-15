package pkb;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import pkb.ast.ConstantNode;
import pkb.ast.ProcedureNode;
import pkb.ast.VariableNode;
import pkb.ast.abstraction.ASTNode;
import pkb.cfg.CFGNode;

public class ProgramKnowledgeBase implements ProgramKnowledgeBaseAPI {

  private final Map<ASTNode, Set<VariableNode>> modifies;

  private final Map<ASTNode, Set<VariableNode>> uses;

  private final Set<VariableNode> varTable;

  private final Set<ConstantNode> constTable;

  private ProcedureNode ast;

  private CFGNode cfg;

  public ProgramKnowledgeBase() {
    modifies = new HashMap<>();
    uses = new HashMap<>();
    varTable = new LinkedHashSet<>();
    constTable = new LinkedHashSet<>();
  }

  public void addAST(ProcedureNode node) {
    ast = node;
  }

  public void addCFG(CFGNode node) {
    cfg = node;
  }

  @Override
  public ProcedureNode getAST() {
    return ast;
  }

  @Override
  public CFGNode getCFG() {
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
  public Set<VariableNode> getVarTable() {
    return varTable;
  }

  @Override
  public Set<ConstantNode> getConstTable() {
    return constTable;
  }

  public void addVariableToVarTable(VariableNode v) {
    varTable.add(v);
  }
  public void addConstantToConstTable(ConstantNode v) {
    constTable.add(v);
  }
}
