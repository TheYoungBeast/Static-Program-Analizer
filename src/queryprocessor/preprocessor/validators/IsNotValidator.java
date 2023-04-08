package queryprocessor.preprocessor.validators;

/***
 * Dekorator Validator
 * Logiczny NOT (np. is not procedure): IsNotValidator(IsProcedureValidator)
 */

public class IsNotValidator implements Validator
{
    public final Validator validator;

    public IsNotValidator(Validator validator) {
        this.validator = validator;
    }

    @Override
    public boolean isValid() {
        return !validator.isValid(); // return negation
    }

    @Override
    public String getErrorMsg() {
        return null;
    }
}
