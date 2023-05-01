package queryprocessor.preprocessor;

import queryprocessor.preprocessor.exceptions.InvalidQueryException;
import queryprocessor.preprocessor.synonyms.NamedProcedureSynonym;
import queryprocessor.preprocessor.synonyms.NamedVariableSynonym;
import queryprocessor.preprocessor.synonyms.Synonym;
import queryprocessor.preprocessor.synonyms.SynonymFactory;

public class ArgumentTypeDeducer
{
    public Synonym<?> deduce(Keyword relType, String arg, int argN) throws InvalidQueryException
    {
        switch (relType)
        {
            case CALLS:
            case T_CALLS:
                if(isUnconstrained(arg))
                    return SynonymFactory.create(arg, Keyword.PROCEDURE);

                return new NamedProcedureSynonym(arg);
            case USES:
            case MODIFIES:
                if (argN == 1)
                    return new NamedProcedureSynonym(arg);

                if(isUnconstrained(arg))
                    return SynonymFactory.create(arg, Keyword.VARIABLE);

                return new NamedVariableSynonym(arg);
            case NEXT:
            case T_NEXT:
                if(isUnconstrained(arg))
                    return SynonymFactory.create(arg, Keyword.PROG_LINE);
        }

        throw new InvalidQueryException(String.format("Cannot deduce type for argument %s in %s", arg, relType.getName()));
    }

    private boolean isUnconstrained(String arg) {
        return arg.trim().equals(Keyword.PLACEHOLDER.getRegExpr());
    }
}
