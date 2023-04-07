package pkb;

import pkb.cfg.CFGNode;
import pkb.ast.ProcedureNode;
import java.util.Set;

public interface ProgramKnowledgeBaseAPI {
    ProcedureNode getAST();

    CFGNode getCFG();

    Set<String> getModifies(int s);

    Set<String> getUses(int s);
}
