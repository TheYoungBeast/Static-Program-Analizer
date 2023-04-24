package queryprocessor.preprocessor.validators;

import queryprocessor.querytree.*;

public class ConditionValidator implements Validator
{
    private final ConditionNode condition;
    private String msg;

    public ConditionValidator(ConditionNode condition) {
        this.condition = condition;
    }

    @Override
    public boolean isValid() {
        if(condition instanceof ConditionRefValue)
        {
            var c = (ConditionRefValue) condition;
            var attr = c.getAttrRef().getAttr();
            var attrValue = c.getAttrValue();

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
        }
        else if(condition instanceof ConditionRefRef) {
            var c = (ConditionRefRef) condition;
            var pair = c.getAttrRefs();
            var ref1 = pair.getFirst();
            var ref2 = pair.getSecond();

            if(ref1.getAttr() != ref2.getAttr()) {
                if(((ref1.getAttr() == AttrName.procName || ref1.getAttr() == AttrName.varName) && (ref2.getAttr() == AttrName.stmtNo || ref2.getAttr() == AttrName.value)) ||
                    ((ref2.getAttr() == AttrName.procName || ref2.getAttr() == AttrName.varName) && (ref1.getAttr() == AttrName.stmtNo || ref1.getAttr() == AttrName.value))) {
                    msg = "Type mismatch. Comparing a NUMBER to a STRING";
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public String getErrorMsg() {
        return msg;
    }
}
