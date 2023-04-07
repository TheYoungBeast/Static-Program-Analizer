package queryprocessor.querytree;

import pkb.ast.abstraction.StatementNode;
import queryprocessor.preprocessor.Synonym;

import java.util.function.Function;

public abstract class Ref extends QTNode{
    public Ref(String label) {
        super(label);
    }

    public abstract Synonym getSynonym();
    public abstract Function<StatementNode, String> getExtractor();
}
