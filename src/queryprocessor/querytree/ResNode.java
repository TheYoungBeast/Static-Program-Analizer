package queryprocessor.querytree;

import queryprocessor.preprocessor.Synonym;

public class ResNode extends QTNode {

  private final Synonym synonym;

  public ResNode(Synonym s) {
    super(s.getIdentifier());
    this.synonym = s;
  }

  public Synonym getSynonym() {
    return synonym;
  }
}
