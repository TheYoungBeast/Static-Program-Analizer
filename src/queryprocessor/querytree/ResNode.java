package queryprocessor.querytree;

import pkb.ast.ProcedureNode;
import pkb.ast.VariableNode;
import pkb.ast.abstraction.ASTNode;
import pkb.ast.abstraction.StatementNode;
import queryprocessor.preprocessor.Synonym;

import java.util.function.Function;

public class ResNode extends QTNode {

  private final Synonym synonym;

  public ResNode(Synonym s) {
    super(s.getIdentifier());
    this.synonym = s;
  }

  public Synonym getSynonym() {
    return synonym;
  }

  public Function<ASTNode, String> getExtractor() {
    return (ASTNode node) -> {
      if(node instanceof ProcedureNode)
        return String.valueOf(((ProcedureNode) node).getName());
      else if(node instanceof VariableNode)
        return String.valueOf(((VariableNode) node).getName());
      else
        return String.valueOf(((StatementNode) node).getStatementId());
    };
  }
}
