package queryprocessor.preprocessor;

import pkb.ast.VariableNode;
import queryprocessor.preprocessor.synonyms.NamedProcedureSynonym;
import queryprocessor.preprocessor.synonyms.NamedVariableSynonym;
import queryprocessor.preprocessor.synonyms.Synonym;
import queryprocessor.preprocessor.synonyms.UnconstrainedSynonym;

public class ArgumentTypeDeducer
{
    public Synonym<?> deduce(Keyword relType, String arg, int argN)
    {
        switch (relType)
        {
            case CALLS:
            case T_CALLS:
                return new NamedProcedureSynonym(arg);
            case USES:
            case MODIFIES:
                if (argN == 1)
                    return new NamedProcedureSynonym(arg);
                else if (argN == 2)
                    return new NamedVariableSynonym(arg);
                else
                    break;
        }

        return  null;
    }
}
