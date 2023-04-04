package queryprocessor.querytree;

import queryprocessor.preprocessor.Keyword;
import queryprocessor.preprocessor.Synonym;

public class RelationshipNode extends QTNode
{
  private final ArgNode arg1;
  private final ArgNode arg2;
  private final Keyword relType;

  public RelationshipNode(Keyword relType, Synonym arg1, Synonym arg2) {
    super(relType.getPattern());
    this.arg1 = new ArgNode(arg1, 1);
    this.arg2 = new ArgNode(arg2, 2);
    this.relType = relType;

    this.arg1.setParent(this);
    this.arg2.setParent(this);
    this.arg1.setRightSibling(this.arg2);
    this.setFirstChild(this.arg1);
  }

  public ArgNode getArg1() {
    return arg1;
  }

  public ArgNode getArg2() {
    return arg2;
  }

  public Keyword getRelationshipType() {
    return relType;
  }
}
