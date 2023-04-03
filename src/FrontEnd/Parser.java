package FrontEnd;

import FrontEnd.AST.*;
import PKB.PKB;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Parser {

  private final List<Token> tokens;

  private final AtomicInteger statementIdGenerator = new AtomicInteger(0);

  private final PKB pkb;

  private int index = 0;

  public Parser(List<Token> tokens, PKB pkb) {
    this.tokens = tokens;
    this.pkb = pkb;
  }

  public ASTNode parse() {
    ASTNode procedureNode = parseProcedure();
    pkb.computeTransitiveClosures();
    pkb.addAST(procedureNode);
    return procedureNode;
  }

  private ProcedureNode parseProcedure() {
    match(TokenType.PROCEDURE);
    String name = match(TokenType.NAME).getValue();
    match(TokenType.LBRACE);
    List<StatementNode> statements = parseStatements();
    match(TokenType.RBRACE);
    return new ProcedureNode(name, statements);
  }

  private List<StatementNode> parseStatements() {
    List<StatementNode> statements = new ArrayList<>();
    StatementNode prevStatement = null;
    while (!check(TokenType.RBRACE)) {
      StatementNode statement = parseStatement();
      statements.add(statement);

      // Add the Follows relation
      if (prevStatement != null) {
        pkb.addFollows(prevStatement.getStatementId(), statement.getStatementId());
        pkb.addCFGEdge(prevStatement.getStatementId(), statement.getStatementId());
      }
      prevStatement = statement;
    }
    return statements;
  }

  private StatementNode parseStatement() {
    int id = statementIdGenerator.incrementAndGet();
    StatementNode statement;
    if (check(TokenType.WHILE)) {
      statement = parseWhile(id);
    } else {
      statement = parseAssignment(id);
    }
    pkb.addCFGNode(statement.getStatementId());
    return statement;
  }

  private WhileNode parseWhile(int id) {
    match(TokenType.WHILE);
    VariableNode condition = (VariableNode) parseExpression();
    match(TokenType.LBRACE);
    // Add the control variable to the Uses relation
    pkb.addUses(id, condition.getName());
    List<StatementNode> statements = parseStatements();
    for (StatementNode statement : statements) {
      pkb.addParent(id, statement.getStatementId());
      // Update Modifies and Uses relations for parent while statement
      updateRelations(statement, id);
    }
    match(TokenType.RBRACE);
    // Add FrontEnd.CFG edge from the while statement to the first statement in its body
    // Add FrontEnd.CFG edge from the last statement in the while body to the while statement
    if (!statements.isEmpty()) {
      pkb.addCFGEdge(id, statements.get(0).getStatementId());
      pkb.addCFGEdge(statements.get(statements.size() - 1).getStatementId(), id);

    }
    return new WhileNode(id, condition, statements);
  }

  private void updateRelations(StatementNode node, int parentId) {
    if (node instanceof AssignmentNode) {
      updateRelationsForAssignment((AssignmentNode) node, parentId);
    } else if (node instanceof WhileNode) {
      updateRelationsForWhile((WhileNode) node, parentId);
    }
  }

  private void updateRelationsForAssignment(AssignmentNode assignmentNode, int parentId) {
    String varName = assignmentNode.getName();
    pkb.addModifies(parentId, varName);
    extractUses(assignmentNode.getExpression(), parentId);
  }

  private void updateRelationsForWhile(WhileNode whileNode, int parentId) {
    pkb.addUses(parentId, whileNode.getCondition().getName());
    for (StatementNode statement : whileNode.getStatements()) {
      updateRelations(statement, parentId);
    }
  }

  private StatementNode parseAssignment(int id) {
    String name = match(TokenType.NAME).getValue();
    match(TokenType.EQUALS);
    ExpressionNode expression = parseExpression();
    match(TokenType.SEMICOLON);
    pkb.addModifies(id, name);
    extractUses(expression, id);
    return new AssignmentNode(id, name, expression);
  }

  private void extractUses(ASTNode node, int id) {
    if (node instanceof VariableNode) {
      pkb.addUses(id, ((VariableNode) node).getName());
    } else if (node instanceof PlusNode) {
      extractUses(((PlusNode) node).getLeft(), id);
      extractUses(((PlusNode) node).getRight(), id);
    }
  }

  private ExpressionNode parseExpression() {
    ExpressionNode left = parseTerm();
    while (check(TokenType.PLUS)) {
      match(TokenType.PLUS);
      ExpressionNode right = parseTerm();
      left = new PlusNode(left, right);
    }
    return left;
  }

  private ExpressionNode parseTerm() {
    if (check(TokenType.CONSTANT)) {
      return new ConstantNode(Integer.parseInt(match(TokenType.CONSTANT).getValue()));
    } else {
      return new VariableNode(match(TokenType.NAME).getValue());
    }
  }

  private Token match(TokenType type) {
    if (check(type)) {
      return tokens.get(index++);
    } else {
      throw new RuntimeException("Unexpected token: " + tokens.get(index).getType());
    }
  }

  private boolean check(TokenType type) {
    if (index < tokens.size()) {
      return tokens.get(index).getType() == type;
    }
    return false;
  }
}