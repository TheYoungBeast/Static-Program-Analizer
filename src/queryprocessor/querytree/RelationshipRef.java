package queryprocessor.querytree;

import queryprocessor.preprocessor.Keyword;
import queryprocessor.preprocessor.Synonym;

import java.util.ArrayList;
import java.util.List;

public class RelationshipRef extends QTNode
{
    private final List<ArgNode> args;
    private final Keyword relType;

    public RelationshipRef(Keyword relType, List<Synonym<?>> argsSynonyms) {
        super(relType.getName());
        this.relType = relType;
        this.args = new ArrayList<>();

        for (int i = 0; i < argsSynonyms.size(); i++)
        {
            var synonym = argsSynonyms.get(i);
            var arg = new ArgNode(synonym, i+1);

            if(!args.isEmpty())
                args.get(args.size()-1).setRightSibling(arg);
            else
                this.setFirstChild(arg);

            arg.setParent(this);
            args.add(arg);
        }
    }

    public int getArgSize() {
        return args.size();
    }

    public ArgNode getArg(int id) {
        return args.get(id);
    }
    public Keyword getRelationshipType() {
        return relType;
    }
}
