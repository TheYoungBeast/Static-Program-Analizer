package queryprocessor.querytree;

import queryprocessor.preprocessor.synonyms.Synonym;

public class ArgNode extends QTNode {

  private final int ArgNum;
  private final Synonym<?> synonym;

  public ArgNode(Synonym<?> s, int ArgNum) {
    super("arg" + ArgNum +": " + s.getIdentifier());
    this.ArgNum = ArgNum;
    this.synonym = s;
  }

  public Synonym<?> getSynonym() {
    return synonym;
  }

  @SuppressWarnings("unused")
  public int getArgNum() {
    return ArgNum;
  }
}
