package QueryProcessor.Preprocessor;

public enum Keyword
{
    SELECT("select"),
    SUCH_THAT("such that"),
    WITH("with"),
    PATTERN("pattern"),
    AND("and"),
    STATEMENT("stmt"),
    ASSIGN("assign"),
    WHILE("while"),
    SYNONYM("[a-zA-Z]+[0-9]*");

    private final String pattern;

    public String getPattern() {
        return this.pattern;
    }

    Keyword(String pattern) {
        this.pattern = pattern;
    }
}
