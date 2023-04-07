package queryprocessor.querytree;

import frontend.ast.abstraction.ASTNode;
import frontend.ast.abstraction.StatementNode;
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
    return (ASTNode node) -> String.valueOf(((StatementNode) node).getStatementId());
  }
}
