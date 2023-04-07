package queryprocessor.preprocessor.validators;

import java.util.regex.Pattern;

public class CallsValidator implements QueryValidator
{
  public static final String regexCall = "\\s+Calls\\s*\\(\\s*(([a-zA-Z]+[0-9]*)|_)+\\s*,\\s*(([a-zA-Z]+[0-9]*)|_)+\\s*\\)\\s+";

  @Override
  public boolean isValid(String query) {
    var matcher = Pattern.compile(regexCall, Pattern.CASE_INSENSITIVE).matcher(query);

    return false;
  }
}
