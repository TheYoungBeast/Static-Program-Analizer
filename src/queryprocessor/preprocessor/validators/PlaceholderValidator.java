package queryprocessor.preprocessor.validators;

@SuppressWarnings("unused")
public class PlaceholderValidator implements Validator {

  @Override
  public boolean isValid() {
    return false;
  }

  @Override
  public String getErrorMsg() {
    return null;
  }
}
