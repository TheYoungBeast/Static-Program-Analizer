package queryprocessor.preprocessor.validators;

import queryprocessor.preprocessor.Keyword;
import queryprocessor.querytree.ArgNode;

public class IsProcedureValidator extends ArgTypeValidator {
    public IsProcedureValidator(ArgNode arg) {
        super(arg, Keyword.PROCEDURE);
    }
}
