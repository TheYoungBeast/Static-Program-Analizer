package queryprocessor.evaluator.abstraction;

import queryprocessor.evaluator.EvaluationResult;
import queryprocessor.querytree.QueryTree;

public interface QueryEvaluator
{
    EvaluationResult evaluate(QueryTree queryTree);
}
