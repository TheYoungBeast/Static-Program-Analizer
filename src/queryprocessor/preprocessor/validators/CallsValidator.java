package queryprocessor.preprocessor.validators;

import queryprocessor.querytree.RelationshipRef;

import java.util.List;

public class CallsValidator implements Validator
{
  private final RelationshipRef rel;
  private final List<Validator> validatorsChain;
  private String lastErrorMsg;

  public CallsValidator(RelationshipRef rel) {
    this.rel = rel;
    this.validatorsChain = ValidatorFactory.createCallsValidatorChain(rel);
  }

  @Override
  public boolean isValid()
  {
    for (var validator: validatorsChain) {
      if (!validator.isValid()) {
        lastErrorMsg = validator.getErrorMsg();
        return false;
      }
    }

    return true;
  }

  @Override
  public String getErrorMsg() {
    return lastErrorMsg;
  }
}
