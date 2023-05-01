package queryprocessor.preprocessor.validators;

import queryprocessor.preprocessor.Keyword;
import queryprocessor.querytree.Condition;
import queryprocessor.querytree.RelationshipRef;

import java.util.ArrayList;
import java.util.List;

/**
 * Do przemyslenia ta fabryka...
 * Albo tworzyć łancuchy validatorów w niej albow konstruktorach odpowiednich klas validatorów relacji
 */

public class ValidatorFactory
{
    public static Validator createRelationshipValidator(RelationshipRef ref)
    {
        Validator validator;
        switch (ref.getRelationshipType())
        {
            case T_PARENT:
            case PARENT:
                validator = new AggregatedValidator(createParentValidatorChain(ref));
                break;
            case T_CALLS:
            case CALLS:
                validator = new AggregatedValidator(createCallsValidatorChain(ref));
                break;
            case USES:
                validator = new AggregatedValidator(createUsesValidatiorChain(ref));
                break;
            case MODIFIES:
                validator = new AggregatedValidator(createModifiesValidatorChain(ref));
                break;
            case T_FOLLOWS:
            case FOLLOWS:
                validator = new AggregatedValidator(createFollowsValidatorChain(ref));
                break;
            case T_AFFECTS:
            case AFFECTS:
                validator = new AggregatedValidator(createAffectValidatorChain(ref));
                break;
            case T_NEXT:
            case NEXT:
                validator = new AggregatedValidator(createNextValidatorChain(ref));
                break;
            default:
                throw new UnsupportedOperationException();
        }

        return validator;
    }

    private static List<Validator> createNextValidatorChain(RelationshipRef ref) {
        var chain = new ArrayList<Validator>();

        final var args = 2;
        chain.add(new ArgumentNumberValidator(ref, args)); // przenies informacje o ilosc arg itp do Statycznej tabeli

        return chain;
    }

    private static List<Validator> createAffectValidatorChain(RelationshipRef ref) {
        var chain = new ArrayList<Validator>();

        final var args = 2;
        chain.add(new ArgumentNumberValidator(ref, args)); // przenies informacje o ilosc arg itp do Statycznej tabeli

        return chain;
    }

    private static List<Validator> createFollowsValidatorChain(RelationshipRef ref) {
        var chain = new ArrayList<Validator>();

        final var args = 2;
        chain.add(new ArgumentNumberValidator(ref, args)); // przenies informacje o ilosc arg itp do Statycznej tabeli

        for (int i = 0; i < ref.getArgSize(); i++ )
            chain.add(new IsAnyValidator(List.of(
                    new ArgTypeValidator(ref.getArg(i), Keyword.STATEMENT),
                    new ArgTypeValidator(ref.getArg(i), Keyword.CALL),
                    new ArgTypeValidator(ref.getArg(i), Keyword.ASSIGN),
                    new ArgTypeValidator(ref.getArg(i), Keyword.IF),
                    new ArgTypeValidator(ref.getArg(i), Keyword.WHILE))
            ));

        return chain;
    }

    private static List<Validator> createModifiesValidatorChain(RelationshipRef ref) {
        var chain = new ArrayList<Validator>();

        final var args = 2;
        chain.add(new ArgumentNumberValidator(ref, args)); // przenies informacje o ilosc arg itp do Statycznej tabeli

        chain.add(new ArgTypeValidator(ref.getArg(1), Keyword.VARIABLE));

        return chain;
    }

    private static List<Validator> createParentValidatorChain(RelationshipRef ref) {
        var chain = new ArrayList<Validator>();

        final var args = 2;
        chain.add(new ArgumentNumberValidator(ref, args)); // przenies informacje o ilosc arg itp do Statycznej tabeli

        chain.add(new IsContainerStatement(ref.getArg(0)));

        var arg = ref.getArg(1);
        chain.add(
                new IsNotValidator(
                        new IsProcedureValidator(arg))
        );

        return chain;
    }

     private static List<Validator> createCallsValidatorChain(RelationshipRef ref)
     {
         var chain = new ArrayList<Validator>();

         final var args = 2;
         chain.add(new ArgumentNumberValidator(ref, args)); // przenies informacje o ilosc arg itp do Statycznej tabeli

         for (int i = 0; i < ref.getArgSize(); i++ )
            chain.add(new IsProcedureValidator(ref.getArg(i)));

         return chain;
     }

     private static List<Validator> createUsesValidatiorChain(RelationshipRef ref) {
        var chain = new ArrayList<Validator>();

         final var args = 2;
         chain.add(new ArgumentNumberValidator(ref, args)); // przenies informacje o ilosc arg itp do Statycznej tabeli

         chain.add(new ArgTypeValidator(ref.getArg(1), Keyword.VARIABLE));

         return chain;
     }

     public static Validator createConditionValidator(Condition node) {
         return new ConditionValidator(node);
     }
}
