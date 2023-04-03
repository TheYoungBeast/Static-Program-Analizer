package FrontEnd.AST;

// Rozdział 16.3
// 16.3 Notes on AST
// Strona 60 [60/69]
// https://cez2.wi.pb.edu.pl/moodle/pluginfile.php/103022/mod_resource/content/4/INF2ATS%20Handbook.pdf

public interface TNode
{
    TNode getFirstChild();
    TNode getRightSibling();
    TNode getUp(); // get parent

    void SetFirstChild(TNode node);
    void SetRightSibling(TNode node);
    void setUp(TNode node);
}
