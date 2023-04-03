package QueryProcessor.Preprocessor.Exceptions;

public class InvalidQueryException extends Exception
{
    private int line = 0;
    private String details = "";

    public InvalidQueryException(String msg) {
        super(msg);
    }

    public InvalidQueryException(String msg, String details) {
        this(msg);
        this.details = details;
    }

    public InvalidQueryException(String msg, int line) {
        this(msg);
        this.line = line;
    }

    public InvalidQueryException(String msg, int line, String details) {
        super(msg);
        this.line = line;
        this.details = details;
    }

    public String explain() {
        return String.format("Invalid Query: Error at line {}: {}", line, details);
    }
}
