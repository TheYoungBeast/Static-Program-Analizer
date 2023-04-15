package queryprocessor.querytree;


public class QTree implements QueryTree
{
    private QTNode resultsNode = null;
    private QTNode suchThatNode = null;
    private QTNode withNode = null;

    public QTree() {
    }

    public void createResultsNode() {
        if(resultsNode == null)
            resultsNode = new ResultNode();

    }
    public void createSuchThatNode() {
        if(suchThatNode == null)
            suchThatNode = new SuchThatNode();

    }
    public void createWithNode() {
        if(withNode == null)
            withNode = new WithNode();

    }

    public void addResNode(ResNode node) {
        if(resultsNode == null)
            this.createResultsNode();

        this.setNode(node, resultsNode);
    }

    public void addRelationshipNode(RelationshipRef node) {
        if(suchThatNode == null)
            this.createSuchThatNode();

        this.setNode(node, suchThatNode);
    }

    public void addConditionNode(ConditionNode node) {
        if(withNode == null)
            this.createWithNode();

        this.setNode(node, withNode);
    }

    private void setNode(QTNode node, QTNode parent)
    {
        if(parent.getFirstChild() == null)
            parent.setFirstChild(node);
        else
        {
            var rNode = parent.getFirstChild();
            while (rNode.getRightSibling() != null)
                rNode = rNode.getRightSibling();

            rNode.setRightSibling(node);
        }

        node.setParent(parent);
    }

    @Override
    public QTNode getResultsNode() {
        return resultsNode;
    }

    @Override
    public QTNode getSuchThatNode() {
        return suchThatNode;
    }

    @Override
    public QTNode getWithNode() {
        return withNode;
    }
}
