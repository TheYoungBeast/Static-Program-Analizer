package queryprocessor.querytree;

import queryprocessor.preprocessor.Synonym;

public class ArgNode extends QTNode {

  private final int ArgNum;
  private final String identifier;
  private final Synonym<?> synonym;

  public ArgNode(Synonym<?> s, int ArgNum) {
    super("arg" + ArgNum +": " + s.getIdentifier());
    this.ArgNum = ArgNum;
    this.identifier = s.getIdentifier();
    this.synonym = s;
  }

  public Synonym<?> getSynonym() {
    return synonym;
  }

  public String getIdentifier() {
    return identifier;
  }

  public int getArgNum() {
    return ArgNum;
  }
}
