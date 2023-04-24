package queryprocessor.preprocessor.validators;

import queryprocessor.preprocessor.Keyword;
import queryprocessor.querytree.ArgNode;

import java.util.ArrayList;
import java.util.List;

public class IsContainerStatement implements Validator
{
    private final IsAnyValidator isAnyValidator;

    public IsContainerStatement(ArgNode arg) {
        var containerType = "Container-type Statement";
        var list = new ArrayList<Validator>(List.of(
                new ArgTypeValidator(arg, Keyword.STATEMENT, containerType),
                new ArgTypeValidator(arg, Keyword.WHILE, containerType),
                new ArgTypeValidator(arg, Keyword.IF, containerType),
                new ArgTypeValidator(arg, Keyword.PROCEDURE, containerType)
        ));
        isAnyValidator = new IsAnyValidator(list);
    }

    @Override
    public boolean isValid() {
        return isAnyValidator.isValid();
    }

    @Override
    public String getErrorMsg() {
        return isAnyValidator.getErrorMsg();
    }
}
