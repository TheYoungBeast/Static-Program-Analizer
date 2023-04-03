package queryprocessor.preprocessor.validators;

import java.util.List;

public class AggregatedValidator implements QueryValidator {

  private final List<QueryValidator> queryValidators;

  public AggregatedValidator(List<QueryValidator> queryValidators) {
    this.queryValidators = queryValidators;
  }

  @Override
  public boolean isValid(String query) {
    for (QueryValidator validator : queryValidators) {
        if (!validator.isValid(query)) {
            return false;
        }
    }

    return true;
  }
}
