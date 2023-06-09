package frontend.parser;

import static frontend.parser.ParseAssignment.parseAssignment;
import static frontend.parser.ParseExpresion.parseExpression;
import static frontend.parser.ParseProgram.parseProgram;

import frontend.lexer.Lexer;
import java.util.ArrayList;
import java.util.Objects;
import pkb.ast.AssignmentNode;
import pkb.ast.CallNode;
import pkb.ast.ConstantNode;
import pkb.ast.IfNode;
import pkb.ast.MinusNode;
import pkb.ast.PlusNode;
import pkb.ast.ProcedureNode;
import pkb.ast.TimesNode;
import pkb.ast.VariableNode;
import pkb.ast.WhileNode;
import pkb.ast.abstraction.ASTNode;
import pkb.ast.abstraction.ContainerNode;
import pkb.ast.abstraction.ExpressionNode;
import pkb.ast.abstraction.StatementNode;
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
    Parser.index = 0;
    pkb.addAST(parseProgram());
  }

  public static ExpressionNode parsePattern(String pattern) {
    Lexer lexer = new Lexer();
    Parser.tokens = lexer.tokenizeFromString(pattern);
    Parser.index = 0;
    return parseExpression();
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

  static boolean isLastToken() {
    return index >= tokens.size();
  }

  static ProcedureNode getProcedureFromPkbOrNew(String name) {
    ProcedureNode procedure = pkb.getProcTable().stream()
        .filter(procedureNode -> Objects.equals(procedureNode.getName(), name))
        .findFirst()
        .orElse(new ProcedureNode(name));
    pkb.addProcedureToProcTable(procedure);
    return procedure;
  }

  static void updateRelations(StatementNode node) {
    if (node instanceof AssignmentNode) {
      updateRelationsForAssignment((AssignmentNode) node);
    } else if (node instanceof WhileNode) {
      updateRelationsForWhile((WhileNode) node);
    } else if (node instanceof IfNode) {
      updateRelationsForIf((IfNode) node);
    } else if (node instanceof CallNode) {
      updateRelationsForCall((CallNode) node);
    }
  }

  private static void updateRelationsForAssignment(AssignmentNode assignmentNode) {
    pkb.addModifies(assignmentNode, assignmentNode.getName());
    pkb.addVariableToVarTable(assignmentNode.getName());
    extractUses(assignmentNode, assignmentNode.getExpression());
  }

  private static void updateRelationsForWhile(WhileNode whileNode) {
    pkb.addUses(whileNode, whileNode.condition);
    pkb.addVariableToVarTable(whileNode.condition);
    updateRelationsFromNestedStatements(whileNode, whileNode.statements);
  }

  private static void updateRelationsForIf(IfNode ifNode) {
    pkb.addUses(ifNode, ifNode.condition);
    pkb.addVariableToVarTable(ifNode.condition);
    updateRelationsFromNestedStatements(ifNode, ifNode.statements);
    updateRelationsFromNestedStatements(ifNode, ifNode.elseStatements);
  }

  private static void updateRelationsForCall(CallNode callNode) {
    pkb.addModifies(callNode);
    pkb.addUses(callNode);
  }

  private static void updateRelationsFromNestedStatements(ContainerNode node, List<StatementNode> statements) {
    for (StatementNode statement : statements) {
      Set<VariableNode> modifies = pkb.getModifies(statement);
      Set<VariableNode> uses = pkb.getUses(statement);
      if (!modifies.isEmpty()) {
        pkb.addModifies(node, modifies);
      }
      if (!uses.isEmpty()) {
        pkb.addUses(node, uses);
      }
    }
  }


  private static void extractUses(AssignmentNode assignmentNode, ExpressionNode node) {
    if (node instanceof VariableNode) {
      pkb.addVariableToVarTable((VariableNode) node);
      pkb.addUses(assignmentNode, (VariableNode) node);
    } else if (node instanceof ConstantNode) {
      pkb.addConstantToConstTable((ConstantNode) node);
    } else if (node instanceof PlusNode) {
      extractUses(assignmentNode, ((PlusNode) node).getLeft());
      extractUses(assignmentNode, ((PlusNode) node).getRight());
    } else if (node instanceof MinusNode) {
      extractUses(assignmentNode, ((MinusNode) node).getLeft());
      extractUses(assignmentNode, ((MinusNode) node).getRight());
    } else if (node instanceof TimesNode) {
      extractUses(assignmentNode, ((TimesNode) node).getLeft());
      extractUses(assignmentNode, ((TimesNode) node).getRight());
    }
  }
}
