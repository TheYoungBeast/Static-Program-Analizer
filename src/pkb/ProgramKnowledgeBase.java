package pkb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import pkb.ast.CallNode;
import pkb.ast.ConstantNode;
import pkb.ast.ProcedureNode;
import pkb.ast.ProgramNode;
import pkb.ast.VariableNode;
import pkb.ast.abstraction.ASTNode;
import pkb.cfg.CFGNode;

public class ProgramKnowledgeBase implements ProgramKnowledgeBaseAPI {

  private final Map<ASTNode, Set<VariableNode>> modifies;

  private final Map<ASTNode, Set<VariableNode>> uses;

  private final Map<ProcedureNode, Set<ProcedureNode>> calls;

  private final Set<CallNode> callNodes;

  private final Set<VariableNode> varTable;

  private final Set<ConstantNode> constTable;

  private ProgramNode ast;

  private List<CFGNode> cfg;

  public ProgramKnowledgeBase() {
    modifies = new HashMap<>();
    uses = new HashMap<>();
    calls = new HashMap<>();
    callNodes = new LinkedHashSet<>();
    varTable = new LinkedHashSet<>();
    constTable = new LinkedHashSet<>();
    cfg = new ArrayList<>();
  }

  public void addAST(ProgramNode node) {
    ast = node;
    calculateCallsRelations();
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

  public void addCallNode(CallNode c) {
    callNodes.add(c);
  }

  private void calculateCallsRelations() {
    for (CallNode call : callNodes) {
      ASTNode procedure = call.getParent();
      while (!(procedure instanceof ProcedureNode)) {
        procedure = procedure.getParent();
      }
      calls.computeIfAbsent((ProcedureNode) procedure, c -> new LinkedHashSet<>()).add(
          ast.procedures.stream()
              .filter(procedureNode -> Objects.equals(procedureNode.getName(), call.getCalledProcedureName()))
              .findFirst()
              .orElse(null)
      );
    }
  }
}
