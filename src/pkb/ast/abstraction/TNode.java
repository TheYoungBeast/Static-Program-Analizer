package pkb.ast.abstraction;

// Rozdział 16.3
// 16.3 Notes on AST
// Strona 60 [60/69]
// https://cez2.wi.pb.edu.pl/moodle/pluginfile.php/103022/mod_resource/content/4/INF2ATS%20Handbook.pdf

public interface TNode {

  TNode getFirstChild();

  TNode getRightSibling();

  TNode getParent();

  void setFirstChild(TNode node);

  void setRightSibling(TNode node);

  void setParent(TNode node);
}
