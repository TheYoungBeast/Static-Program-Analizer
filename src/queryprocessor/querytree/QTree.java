package queryprocessor.querytree;


public class QTree implements QueryTree {

  private QTNode resultNode = null;
  private QTNode suchThatNode = null;
  private QTNode withNode = null;

  public QTree() {
  }

  public void setResultNode(ResultNode rn) {
    this.resultNode = rn;
  }
  public void setSuchThatNode(SuchThatNode stn) {
    this.suchThatNode = stn;
  }
  public void setWithNode(WithNode wthn) {
    this.withNode = wthn;
  }

  @Override
  public QTNode getResultNode() {
    return resultNode;
  }

  @Override
  public QTNode getSuchThatNode() {
    return suchThatNode;
  }

  @Override
  public QTNode getWithNode() {
    return withNode;
  }
}
