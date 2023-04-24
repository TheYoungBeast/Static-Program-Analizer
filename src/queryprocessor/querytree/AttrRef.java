package queryprocessor.querytree;

import queryprocessor.preprocessor.synonyms.Synonym;

public class AttrRef extends QTNode
{
    private final Synonym<?> synonym;
    private final AttrName attrName;

    public AttrRef(Synonym<?> s, AttrName a) {
        super("AttrRef: " + s.getIdentifier() + "." + a.getName());
        this.synonym = s;
        this.attrName = a;
    }

    public Synonym<?> getSynonym() {
        return synonym;
    }

    public AttrName getAttr() {
        return attrName;
    }
}
