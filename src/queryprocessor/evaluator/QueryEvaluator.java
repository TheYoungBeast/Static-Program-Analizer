package queryprocessor.evaluator;

import pkb.ast.abstraction.ASTNode;
import queryprocessor.querytree.QueryTree;

import java.util.List;

public interface QueryEvaluator
{
    List<ASTNode> evaluate(QueryTree queryTree);
}
