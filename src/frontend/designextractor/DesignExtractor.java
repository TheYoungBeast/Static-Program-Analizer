package frontend.designextractor;

import frontend.parser.Parser;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import pkb.ProgramKnowledgeBase;
import pkb.ast.CallNode;
import pkb.ast.ProcedureNode;
import pkb.ast.ProgramNode;
import pkb.ast.VariableNode;
import pkb.ast.abstraction.ASTNode;
import pkb.ast.abstraction.ContainerNode;
import pkb.ast.abstraction.StatementNode;

public class DesignExtractor {

  private static ProgramKnowledgeBase pkb;

  public static void extract(ProgramKnowledgeBase pkb) {
    DesignExtractor.pkb = pkb;
    calculateCallsRelations();
    calculateModifiesAndUsesForProcedure();
  }

  private static void calculateCallsRelations() {
    for (ProcedureNode procedure : pkb.getProcTable()) {
      List<StatementNode> statements = procedure.getStatements();
      for (int i = 0; i < statements.size() - 1; ++i) {
        if (statements.get(i) instanceof ContainerNode) {
          statements.addAll(((ContainerNode) statements.get(i)).getStatements());
        } else if (statements.get(i) instanceof CallNode) {
          pkb.getCalls().computeIfAbsent(procedure, c -> new LinkedHashSet<>())
              .add(((CallNode) statements.get(i)).getCalledProcedure());
        }
      }
    }
  }

  private static void calculateModifiesAndUsesForProcedure() {
    for (ProcedureNode procedure : pkb.getProcTable()) {
      extractRelations(procedure, pkb.getModifies(), pkb::addModifies);
      extractRelations(procedure, pkb.getUses(), pkb::addUses);
    }
    for (ProcedureNode procedure : pkb.getProcTable()) {
      for (ProcedureNode calledProcedure : pkb.getCalls(procedure)) {
        pkb.addModifies(procedure, pkb.getModifies(calledProcedure));
        pkb.addUses(procedure, pkb.getUses(calledProcedure));
      }
    }
    for (ASTNode node : pkb.getModifies().keySet()) {
      if (node instanceof CallNode) {
        pkb.addModifies(node, pkb.getModifies(((CallNode) node).getCalledProcedure()));
        ASTNode parent = node.getParent();
        do {
            pkb.addModifies(parent, pkb.getModifies(node));
            parent = parent.getParent();
        } while (!(parent instanceof ProgramNode));
      }
    }
    for (ASTNode node : pkb.getUses().keySet()) {
      if (node instanceof CallNode) {
        pkb.addUses(node, pkb.getUses(((CallNode) node).getCalledProcedure()));
        ASTNode parent = node.getParent();
        do {
          pkb.addModifies(parent, pkb.getUses(node));
          parent = parent.getParent();
        } while (!(parent instanceof ProgramNode));
      }
    }
  }

  private static void extractRelations(ProcedureNode procedure,
      Map<ASTNode, Set<VariableNode>> relation, BiConsumer<ProcedureNode, Set<VariableNode>> add) {
    for (ASTNode node : relation.keySet()) {
      if (procedure.getStatements().contains(node)) {
        add.accept(procedure, relation.get(node));
      }
    }
  }
}
