package queryprocessor.preprocessor.validators;

import queryprocessor.preprocessor.Keyword;
import queryprocessor.querytree.ArgNode;

public class ArgTypeValidator implements Validator
{
    private final Keyword expectedType;
    private final String expectedTypeStr;
    private final ArgNode arg;
    private boolean valid = true;

    public ArgTypeValidator(ArgNode arg, Keyword expectedType, String expectedTypeStr) {
        this.arg = arg;
        this.expectedType = expectedType;
        this.expectedTypeStr = expectedTypeStr;
    }

    public ArgTypeValidator(ArgNode arg, Keyword expectedType) {
        this(arg, expectedType, expectedType.getName());
    }

    @Override
    public boolean isValid() {
        valid = arg.getSynonym().getKeyword() == expectedType;
        return valid;
    }

    @Override
    public String getErrorMsg() {
        if(valid)
            return null;

        return String.format("Invalid type for argument \"%s\". Expected %s got %s",
                arg.getSynonym().getIdentifier(),
                expectedTypeStr,
                arg.getSynonym().getKeyword().getName());
    }
}
