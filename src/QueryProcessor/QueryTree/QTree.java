package QueryProcessor.QueryTree;


public class QTree implements QueryTree
{
    private final QTNode resultNode;

    public QTree(QTNode resultNode) {
        this.resultNode = resultNode;
    }

    @Override
    public QTNode getResultNode() {
        return resultNode;
    }
}
