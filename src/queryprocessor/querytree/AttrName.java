package queryprocessor.querytree;

import queryprocessor.preprocessor.Keyword;

public enum AttrName
{
    procName(Keyword.PROCNAME.getRegExpr()),
    varName(Keyword.VARNAME.getRegExpr()),
    value(Keyword.VALUE.getRegExpr()),
    stmtNo(Keyword.STMTNUMBER.getRegExpr());

    private final String attrName;

    AttrName(String attr) {
        this.attrName = attr;
    }

    public String getName() {
        return attrName;
    }
}
