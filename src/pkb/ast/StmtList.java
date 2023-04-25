package pkb.ast;

import pkb.ast.abstraction.ASTNode;
import pkb.ast.abstraction.StatementNode;

import java.util.List;

public class StmtList extends ASTNode {
    public StmtList(ASTNode Parent, List<StatementNode> stmtList) {
        this.setParent(Parent);

        var last = stmtList.stream().findFirst().orElse(null);
        this.setFirstChild(last);

        for (var stmt : stmtList) {
            stmt.setParent(Parent);

            if (last != null)
                last.setRightSibling(stmt);

            last = stmt;
        }

        stmtList.get(stmtList.size()-1).setRightSibling(null);
    }
}
