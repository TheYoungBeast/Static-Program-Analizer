package queryprocessor.preprocessor.validators;

import queryprocessor.querytree.RelationshipRef;

public class ArgumentNumberValidator implements Validator
{
    private final RelationshipRef relRef;
    private final int assertNumber;
    private final int argNumber;
    private boolean valid = true;

    public ArgumentNumberValidator(RelationshipRef relRef, int number) {
        this.relRef = relRef;
        this.assertNumber = number;
        this.argNumber = relRef.getArgSize();
    }

    @Override
    public boolean isValid() {
        valid = relRef.getArgSize() == assertNumber;
        return valid;
    }

    @Override
    public String getErrorMsg() {
        if(valid)
            return null;

        return String.format("Invalid number of arguments. Required %d got %d", assertNumber, argNumber);
    }
}
