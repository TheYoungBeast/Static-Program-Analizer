package queryresultprojector;

import pkb.ast.abstraction.ASTNode;
import utils.Pair;

import java.util.List;
import java.util.function.Function;

public class QueryResultProjector
{
    private List<Pair<ASTNode, Function<ASTNode, String>>> resultPairs;

    public void setResultPairs(List<Pair<ASTNode, Function<ASTNode, String>>> r) {
        this.resultPairs = r;
    }

    public String format() {
        var builder = new StringBuilder();

        for (var pair: resultPairs) {
            builder.append(pair.getSecond().apply(pair.getFirst()));
            builder.append(System.getProperty("line.separator"));
        }

        return builder.toString();
    }
}
