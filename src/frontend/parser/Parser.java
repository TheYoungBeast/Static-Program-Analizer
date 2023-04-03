package frontend.parser;

import static frontend.parser.ParseProcedure.parseProcedure;

import frontend.ast.AssignmentNode;
import frontend.ast.PlusNode;
import frontend.ast.VariableNode;
import frontend.ast.WhileNode;
import frontend.ast.abstraction.ExpressionNode;
import frontend.ast.abstraction.StatementNode;
import frontend.lexer.Token;
import frontend.lexer.TokenType;
import java.util.List;
import java.util.Set;
import pkb.ProgramKnowledgeBase;


public class Parser {

  private static List<Token> tokens;

  private static int index = 0;

  private static ProgramKnowledgeBase pkb;

  /**
   * Create AST tree.
   */
  public static void parse(List<Token> tokens, ProgramKnowledgeBase pkb) {
    Parser.tokens = tokens;
    Parser.pkb = pkb;
    pkb.addAST(parseProcedure());
  }

  static Token match(TokenType type) {
    if (check(type)) {
      return tokens.get(index++);
    } else {
      throw new RuntimeException("Unexpected token: " + tokens.get(index).getType());
    }
  }

  static boolean check(TokenType type) {
    if (index < tokens.size()) {
      return tokens.get(index).getType() == type;
    }
    return false;
  }

  static void updateRelations(StatementNode node) {
    if (node instanceof AssignmentNode) {
      updateRelationsForAssignment((AssignmentNode) node);
    } else if (node instanceof WhileNode) {
      updateRelationsForWhile((WhileNode) node);
    }
  }

  private static void updateRelationsForAssignment(AssignmentNode assignmentNode) {
    pkb.addModifies(assignmentNode.getStatementId(), assignmentNode.getName());
    extractUses(assignmentNode.getStatementId(), assignmentNode.getExpression());
  }

  private static void updateRelationsForWhile(WhileNode whileNode) {
    pkb.addUses(whileNode.getStatementId(), whileNode.condition.getName());
    for (StatementNode statement : whileNode.statements) {
      Set<String> modifies = pkb.getModifies(statement.getStatementId());
      Set<String> uses = pkb.getUses(statement.getStatementId());
      if (!modifies.isEmpty()) {
        pkb.addModifies(whileNode.getStatementId(), modifies);
      }
      if (!uses.isEmpty()) {
        pkb.addUses(whileNode.getStatementId(), uses);
      }
    }
  }

  private static void extractUses(int id, ExpressionNode node) {
    if (node instanceof VariableNode) {
      pkb.addUses(id, ((VariableNode) node).getName());
    } else if (node instanceof PlusNode) {
      extractUses(id, ((PlusNode) node).getLeft());
      extractUses(id, ((PlusNode) node).getRight());
    }
  }
}
