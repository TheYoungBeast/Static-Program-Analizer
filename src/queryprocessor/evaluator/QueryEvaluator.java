package queryprocessor.evaluator;

import queryprocessor.querytree.QueryTree;

public interface QueryEvaluator
{
    EvaluationResult evaluate(QueryTree queryTree);
}
