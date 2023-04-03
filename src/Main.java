import FrontEnd.AST.ASTNode;
import FrontEnd.AST.TNode;
import FrontEnd.Lexer;
import FrontEnd.Parser;
import FrontEnd.Token;
import PKB.PKB;
import QueryProcessor.Preprocessor.QTNode;
import QueryProcessor.Preprocessor.QueryPreprocessorBase;
import QueryProcessor.QueryTree;

import java.util.List;
import java.util.regex.Pattern;

public class Main {

  public static void main(String[] args) throws Exception {
    Lexer lexer = new Lexer();
    List<Token> tokens = lexer.tokenize("example_source_code.txt");
    PKB pkb = new PKB();
    Parser parser = new Parser(tokens, pkb);
    ASTNode ast = parser.parse();
    System.out.println(ast);

    var qp = new QueryPreprocessorBase();

    QueryTree qt = qp.parseQuery("assign a1, a2; while w; select a1, a2");

    var node = qt.getResultNode();
    var sib = qt.getResultNode().getRightSibling();

    while(node != null || sib != null) {
      System.out.println(node.getLabel());
      sib = node.getRightSibling();
      node = node.getFirstChild();

      if(node == null)
        node = sib;
    }
  }
}
