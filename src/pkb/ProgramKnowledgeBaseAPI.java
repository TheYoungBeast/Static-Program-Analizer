package pkb;

import java.util.List;
import pkb.ast.ConstantNode;
import pkb.ast.ProgramNode;
import pkb.ast.VariableNode;
import pkb.ast.abstraction.ASTNode;
import pkb.cfg.CFGNode;
import pkb.ast.ProcedureNode;
import java.util.Set;

@SuppressWarnings("unused")
public interface ProgramKnowledgeBaseAPI {
    ProgramNode getAST();

    List<CFGNode> getCFG();

    Set<VariableNode> getModifies(ASTNode s);

    Set<VariableNode> getUses(ASTNode s);

    Set<ProcedureNode> getCalls(ASTNode p);

    Set<VariableNode> getVarTable();

    Set<ConstantNode> getConstTable();
}
