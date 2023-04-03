package QueryProcessor;

import QueryProcessor.Preprocessor.QTNode;

public class QueryTreeImpl implements QueryTree
{
    private final QTNode resultNode;

    public QueryTreeImpl(QTNode resultNode) {
        this.resultNode = resultNode;
    }

    @Override
    public QTNode getResultNode() {
        return resultNode;
    }
}
