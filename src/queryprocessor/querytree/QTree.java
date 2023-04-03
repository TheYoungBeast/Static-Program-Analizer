package queryprocessor.querytree;


public class QTree implements QueryTree {

  private QTNode resultNode = null;
  private QTNode suchThatNode = null;
  private final QTNode withNode = null;

  public QTree() {
  }

  public void setResultNode(ResultNode rn) {
    this.resultNode = rn;
  }
  public void setSuchThatNode(SuchThatNode stn) {
    this.suchThatNode = stn;
  }

  @Override
  public QTNode getResultNode() {
    return resultNode;
  }

  @Override
  public QTNode getSuchThatNode() {
    return null;
  }

  @Override
  public QTNode getWithNode() {
    return null;
  }
}
