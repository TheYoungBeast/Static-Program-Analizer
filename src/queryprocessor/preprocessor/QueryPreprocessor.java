package queryprocessor.preprocessor;

import queryprocessor.preprocessor.exceptions.InvalidQueryException;
import queryprocessor.preprocessor.exceptions.MissingArgumentException;
import queryprocessor.preprocessor.synonyms.Synonym;
import queryprocessor.querytree.QueryTree;

public interface QueryPreprocessor {

    QueryTree parseQuery(String query) throws InvalidQueryException, MissingArgumentException;

    QueryTree parseQuery(String[] queryLines) throws InvalidQueryException, MissingArgumentException;

    Synonym<?> getDeclaredSynonym(String id);
}
