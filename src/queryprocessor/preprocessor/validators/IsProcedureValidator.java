package queryprocessor.preprocessor.validators;

import queryprocessor.preprocessor.Keyword;
import queryprocessor.querytree.ArgNode;

public class IsProcedureValidator implements Validator
{
    private final Keyword expectedType = Keyword.PROCEDURE;
    private final ArgNode arg;
    private boolean valid = true;

    public IsProcedureValidator(ArgNode arg) {
        this.arg = arg;
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

        return String.format("Invalid \"%s\" argument type. Expected %s got %s",
                arg.getSynonym().getIdentifier(),
                expectedType.getName(),
                arg.getSynonym().getKeyword().getName());
    }
}
