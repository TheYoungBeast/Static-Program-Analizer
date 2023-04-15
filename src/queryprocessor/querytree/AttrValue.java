package queryprocessor.querytree;

public class AttrValue extends QTNode
{
    public enum ValueType {
        NUMBER,
        STRING
    }

    private final ValueType valueType;
    private final String value;

    public AttrValue(String value) {
        super("AttrValue");
        this.value = value;
        ValueType t;
        try {
            Long.parseLong(value);
            t = ValueType.NUMBER;
        }
        catch (NumberFormatException e) { t = ValueType.STRING; }
        this.valueType = t;
    }

    public String getValue() {
        return value;
    }

    public ValueType getValueType() {
        return valueType;
    }
}
