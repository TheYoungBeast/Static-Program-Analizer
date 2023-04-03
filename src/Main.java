import frontend.lexer.Lexer;
import frontend.lexer.Token;
import frontend.parser.Parser;
import java.util.List;
import java.util.Stack;
import pkb.ProgramKnowledgeBase;
import queryprocessor.preprocessor.QueryPreprocessorBase;
import queryprocessor.querytree.QTNode;
import queryprocessor.querytree.QueryTree;

public class Main {

  public static void main(String[] args) throws Exception {
    Lexer lexer = new Lexer();
    List<Token> tokens = lexer.tokenize("example_source_code.txt");
    ProgramKnowledgeBase pkb = new ProgramKnowledgeBase();
    Parser.parse(tokens, pkb);
    System.out.println(pkb.getAST());

    var qp = new QueryPreprocessorBase();

    QueryTree qt = qp.parseQuery("assign a1, a2; while w; select a1, a2 such that Parent");

    var node = qt.getResultNode();

    // trawersowanie drzewa QTNode / TNode / ASTNode
    Stack<QTNode> nodeStack = new Stack<>();
    do {
      if(node == null) {
        if(!nodeStack.empty())
          node = nodeStack.pop();
        continue;
      }
      nodeStack.add(node.getRightSibling());
      System.out.println(node.getLabel());
      node = node.getFirstChild();
    } while(!nodeStack.empty());
  }
}
