package pkb;

import pkb.ast.VariableNode;
import pkb.ast.abstraction.ASTNode;
import pkb.ast.abstraction.StatementNode;
import pkb.cfg.CFGNode;
import pkb.ast.ProcedureNode;
import java.util.Set;

public interface ProgramKnowledgeBaseAPI {
    ProcedureNode getAST();

    CFGNode getCFG();

    Set<VariableNode> getModifies(ASTNode s);

    Set<VariableNode> getUses(ASTNode s);
}
