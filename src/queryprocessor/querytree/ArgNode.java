package queryprocessor.querytree;

public class ArgNode extends QTNode {

  private final int ArgNum;
  private final String identifier;

  public ArgNode(String id, int ArgNum) {
    super("arg" + ArgNum +": " + id);
    this.ArgNum = ArgNum;
    this.identifier = id;
  }

  public String getIdentifier() {
    return identifier;
  }

  public int getArgNum() {
    return ArgNum;
  }
}
