package queryprocessor.querytree;

import queryprocessor.preprocessor.Synonym;

public class ConditionNode extends QTNode
{
    private final String condValue;

    public ConditionNode(AttrRef ref, String condValue) {
        super("Attr: " + ref.getSynonym().getIdentifier()+"."+ref.getAttrName().getName() + " = " + condValue);
        ref.setParent(this);
        this.setFirstChild(ref);

        this.condValue = condValue;
    }
}
