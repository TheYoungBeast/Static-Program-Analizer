package queryprocessor.evaluator;

import pkb.ast.abstraction.ASTNode;
import queryprocessor.querytree.QueryTree;
import utils.Pair;

import java.util.List;
import java.util.function.Function;

public interface QueryEvaluator
{
    List<Pair<ASTNode, Function<ASTNode, String>>> evaluate(QueryTree queryTree);
}
