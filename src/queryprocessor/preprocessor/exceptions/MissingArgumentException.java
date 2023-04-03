package queryprocessor.preprocessor.exceptions;

public class MissingArgumentException extends Exception implements QueryException {

  public MissingArgumentException(String msg) {
    super(msg);
  }

  @Override
  public String explain() {
    return super.getMessage();
  }
}
