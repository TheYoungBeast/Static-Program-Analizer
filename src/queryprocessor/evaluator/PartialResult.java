package queryprocessor.evaluator;

import pkb.ast.abstraction.ASTNode;
import queryprocessor.evaluator.abstraction.PartialResultAbstract;
import queryprocessor.preprocessor.synonyms.Synonym;
import utils.Pair;

import java.util.Set;

public class PartialResult extends PartialResultAbstract<Synonym<?>, ASTNode, Set<?>, Pair<Synonym<?>, Synonym<?>>> {
    public PartialResult(Synonym<?> key, Set<? super ASTNode> value) {
        super(key, value);
    }

    public PartialResult(Pair<Synonym<?>, Synonym<?>> keyPair, Set<? extends Pair<? super ASTNode, ? super ASTNode>> value) {
        super(keyPair, value);
    }
}
