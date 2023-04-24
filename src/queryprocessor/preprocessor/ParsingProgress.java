package queryprocessor.preprocessor;

public class ParsingProgress
{
    private boolean[] status;
    private String  query;

    public ParsingProgress(String query) {
        status = new boolean[query.length()];
        this.query = query;
    }

    public void setParsed(int start, int end) {
        for (int i = start; i < end; i++) {
            status[i] = true;
        }
    }

    public boolean isCompleted() {
        for (int i = 0; i < status.length; i++) {
            if (!status[i] && !Character.isWhitespace(query.charAt(i)))
                return false;
        }

        return true;
    }
}
