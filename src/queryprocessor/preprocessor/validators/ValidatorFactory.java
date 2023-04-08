package queryprocessor.preprocessor.validators;

import queryprocessor.querytree.RelationshipRef;

import java.util.ArrayList;
import java.util.List;

/**
 * Do przemyslenia ta fabryka...
 * Albo tworzyć łancuchy validatorów w niej albow konstruktorach odpowiednich klas validatorów relacji
 */

public class ValidatorFactory
{
     public static List<Validator> createCallsValidatorChain(RelationshipRef ref)
     {
         var chain = new ArrayList<Validator>();

         chain.add(new ArgumentNumberValidator(ref, 2)); // przenies informacje o ilosc arg itp do Statycznej tabeli

         for (int i = 0; i < ref.getArgSize(); i++ )
            chain.add(new IsProcedureValidator(ref.getArg(i)));

         return chain;
     }
}
