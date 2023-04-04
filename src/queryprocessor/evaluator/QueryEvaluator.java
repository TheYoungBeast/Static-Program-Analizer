package queryprocessor.evaluator;

import frontend.ast.abstraction.ASTNode;
import queryprocessor.querytree.QueryTree;

import java.util.List;

public interface QueryEvaluator
{
    List<ASTNode> evaluate(QueryTree queryTree);
}
