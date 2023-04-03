package QueryProcessor;

// Rozdział 16.8.2
// 16.8.2 Query Preprocessor
// Rys. 24 A query tree
// Strona 62 [62/69]
// https://cez2.wi.pb.edu.pl/moodle/pluginfile.php/103022/mod_resource/content/4/INF2ATS%20Handbook.pdf

import QueryProcessor.Preprocessor.QTNode;

public interface QueryTree
{
    QTNode getResultNode();
}
