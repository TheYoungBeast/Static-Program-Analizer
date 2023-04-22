package pkb.cfg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import pkb.ProgramKnowledgeBase;
import pkb.ast.ProcedureNode;
import pkb.ast.ProgramNode;
import pkb.ast.WhileNode;
import pkb.ast.abstraction.StatementNode;

public class ControlFlowGraph {

  private static Map<Integer, CFGNode> cfgNodes;

  public static void createCfg(ProgramKnowledgeBase pkb) {
    cfgNodes = new HashMap<>();
    ProgramNode ast = pkb.getAST();
    for (ProcedureNode procedure : ast.procedures) {
      CFGNode startingNode = getOrCreateCfgNode(procedure.statements.get(0).getStatementId());
      generateCfgFromAst(procedure.statements, startingNode);
      pkb.addCFG(startingNode);
    }
  }

  private static CFGNode getOrCreateCfgNode(int statementId) {
    cfgNodes.putIfAbsent(statementId, new CFGNode(statementId));
    return cfgNodes.get(statementId);
  }

  private static void generateCfgFromAst(List<StatementNode> astNodes, CFGNode currentNode) {
    int index = 0;

    while (index < astNodes.size()) {
      if (astNodes.get(index) instanceof WhileNode) {
        WhileNode whileNode = (WhileNode) astNodes.get(index);
        CFGNode firstStmtInWhile = getOrCreateCfgNode(whileNode.statements.get(0).getStatementId());
        currentNode.addSuccessor(firstStmtInWhile);
        generateCfgFromAst(whileNode.statements, firstStmtInWhile);
      }

      if (index >= astNodes.size() - 1) {
        CFGNode last = getOrCreateCfgNode(astNodes.get(index).getStatementId());
        if (!(astNodes instanceof ProcedureNode) && astNodes instanceof StatementNode) {
          last.addSuccessor(getOrCreateCfgNode(((StatementNode) astNodes.get(index).getParent()).getStatementId()));
        }
        return;
      }

      CFGNode nextNode = getOrCreateCfgNode(astNodes.get(index + 1).getStatementId());
      currentNode.addSuccessor(nextNode);

      currentNode = nextNode;
      index++;
    }
  }
}