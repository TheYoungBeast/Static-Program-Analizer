package queryprocessor.querytree;

import pkb.ast.abstraction.ASTNode;
import utils.Pair;

public class ConditionRefRef extends Condition
{
    AttrRef attrRef1;
    AttrRef attrRef2;

    public ConditionRefRef(AttrRef attrRef1, AttrRef attrRef2) {
        super("Condition ref.attr = ref.attr");
        this.attrRef1 = attrRef1;
        this.attrRef2 = attrRef2;
    }

    @Override
    public boolean attrCompare(Object o) {
        if(!(o instanceof Pair))
            return false;

        Pair<ASTNode, ASTNode> pair = (Pair<ASTNode, ASTNode>) o;
        return attrRef1.extractAttributeValue(pair.getFirst())
                .equals(attrRef2.extractAttributeValue(pair.getSecond()));
    }

    public Pair<AttrRef, AttrRef> getAttrRefs() {
        return new Pair<>(attrRef1, attrRef2);
    }
}
