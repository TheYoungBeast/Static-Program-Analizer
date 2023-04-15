package pkb;

import pkb.ast.ConstantNode;
import pkb.ast.VariableNode;
import pkb.ast.abstraction.ASTNode;
import pkb.cfg.CFGNode;
import pkb.ast.ProcedureNode;
import java.util.Set;

@SuppressWarnings("unused")
public interface ProgramKnowledgeBaseAPI {
    ProcedureNode getAST();

    CFGNode getCFG();

    Set<VariableNode> getModifies(ASTNode s);

    Set<VariableNode> getUses(ASTNode s);

    Set<VariableNode> getVarTable();

    Set<ConstantNode> getConstTable();
}
