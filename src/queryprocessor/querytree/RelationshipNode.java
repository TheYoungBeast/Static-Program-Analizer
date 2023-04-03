package queryprocessor.querytree;

public class RelationshipNode extends QTNode
{
  private final ArgNode arg1;
  private final ArgNode arg2;

  public RelationshipNode(String label, String arg1, String arg2) {
    super(label);
    this.arg1 = new ArgNode(arg1, 1);
    this.arg2 = new ArgNode(arg2, 2);
  }
}
