import cfg.ControlFlowGraph;
import frontend.ast.AssignmentNode;
import frontend.ast.WhileNode;
import frontend.lexer.Lexer;
import frontend.lexer.Token;
import frontend.parser.Parser;
import java.util.List;
import java.util.Stack;
import pkb.ProgramKnowledgeBase;
import queryprocessor.evaluator.QueryEvaluator;
import queryprocessor.evaluator.QueryEvaluatorBase;
import queryprocessor.preprocessor.QueryPreprocessorBase;
import queryprocessor.preprocessor.exceptions.InvalidQueryException;
import queryprocessor.preprocessor.exceptions.MissingArgumentException;
import queryprocessor.querytree.QTNode;
import queryprocessor.querytree.QueryTree;

public class Main {

  public static void main(String[] args) {
    Lexer lexer = new Lexer();
    List<Token> tokens = lexer.tokenize("example_source_code.txt");
    ProgramKnowledgeBase pkb = new ProgramKnowledgeBase();
    Parser.parse(tokens, pkb);
    ControlFlowGraph.createCfg(pkb);
    System.out.println(pkb.getAST());

    var qp = new QueryPreprocessorBase();

    QueryTree qt = null;
    try {
//      qt = qp.parseQuery("stmt s1,s2; while w; select s1, s2 such that Parent(s1, w) with s1.stmt#=2;");
      qt = qp.parseQuery("assign w; select w;");
    } catch (InvalidQueryException e) {
      System.err.println(e.explain());
    } catch (MissingArgumentException e) {
      System.err.println(e.explain());
    }

    if(qt == null)
      return;

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
    } while(!nodeStack.empty() || node != null);

    QueryEvaluator evaluator = new QueryEvaluatorBase(pkb);
    var list = evaluator.evaluate(qt);

    // QUERY FORMAT PROJECTOR - DO NAPISANIA - FORMATOWANIE REZULTATOW
    System.out.println("\nResults:\n");
    for (var r: list) {
      System.out.println(((AssignmentNode)r).getStatementId()+1);
    }
  }
}
