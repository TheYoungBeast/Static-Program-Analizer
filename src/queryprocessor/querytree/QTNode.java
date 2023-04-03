package queryprocessor.querytree;

import frontend.ast.abstraction.TNode;

public class QTNode implements TNode {

  protected final String label;

  protected QTNode firstChild = null;

  protected QTNode rightSibling = null;

  protected QTNode up = null;

  public QTNode(String label) {
    this.label = label;

  }

  public String getLabel() {
    return label;
  }

  @Override
  public QTNode getFirstChild() {
    return firstChild;
  }

  @Override
  public QTNode getRightSibling() {
    return rightSibling;
  }

  @Override
  public QTNode getParent() {
    return up;
  }

  @Override
  public void SetFirstChild(TNode node) {
    firstChild = (QTNode) node;
  }

  @Override
  public void SetRightSibling(TNode node) {
    rightSibling = (QTNode) node;
  }

  @Override
  public void setParent(TNode node) {
    up = (QTNode) node;
  }
}

