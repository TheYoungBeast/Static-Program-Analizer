package queryprocessor.preprocessor.validators;

import queryprocessor.querytree.AttrName;
import queryprocessor.querytree.AttrValue;
import queryprocessor.querytree.ConditionNode;

public class ConditionValidator implements Validator
{
    private final ConditionNode condition;
    private String msg;

    public ConditionValidator(ConditionNode condition) {
        this.condition = condition;
    }

    @Override
    public boolean isValid() {
        var attr = condition.getAttrRef().getAttr();
        var attrValue = condition.getAttrValue();

        if(attr.equals(AttrName.value) || attr.equals(AttrName.stmtNo)) {
            if (attrValue.getValueType() != AttrValue.ValueType.NUMBER) {
                msg = "Type mismatch. Comparing a STRING to a NUMBER";
                return false;
            }
        }
        else {
            if(attrValue.getValueType() != AttrValue.ValueType.STRING) {
                msg = "Type mismatch. Comparing a NUMBER to a STRING";
                return false;
            }
        }

        return true;
    }

    @Override
    public String getErrorMsg() {
        return msg;
    }
}
