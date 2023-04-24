package queryprocessor.preprocessor;


public class ArgumentTypeDeducer
{
    public Synonym<?> deduce(Keyword relType, String arg, int argN) {

        if(relType == Keyword.CALLS || relType == Keyword.T_CALLS)
            return new NamedProcedureSynonym(arg);
        else if(relType == Keyword.USES || relType == Keyword.MODIFIES) {
            if(argN == 1)
                return new NamedProcedureSynonym(arg);
            else if(argN == 2)
                return new NamedVariableSynonym(arg);
        }

        return  null;
    }
}
