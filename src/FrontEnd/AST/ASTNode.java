package FrontEnd.AST;

import java.util.List;

// rozdział 16.3 Handbook
public class ASTNode implements TNode {
  private ASTNode firstchild;
  private ASTNode rightSibling;
  private ASTNode up;

  public ASTNode() {
    firstchild = null;
    rightSibling = null;
    up = null;
  }

  @Override
  public ASTNode getFirstChild() {
    return firstchild;
  }

  @Override
  public ASTNode getRightSibling() {
    return rightSibling;
  }

  @Override
  public ASTNode getUp() {
    return up;
  }

  @Override
  public void SetFirstChild(TNode node) {
    firstchild = (ASTNode) node;
  }

  @Override
  public void SetRightSibling(TNode node) {
    rightSibling = (ASTNode) node;
  }

  @Override
  public void setUp(TNode node) {
    up = (ASTNode) node;
  }
}

