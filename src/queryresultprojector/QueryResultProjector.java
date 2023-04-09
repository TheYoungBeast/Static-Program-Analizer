package queryresultprojector;

import pkb.ast.abstraction.ASTNode;
import queryprocessor.evaluator.EvaluationResult;
import utils.CartesianProduct;

import java.util.Map;
import java.util.stream.Collectors;


public class QueryResultProjector
{
    private EvaluationResult evaluationResult;

    public void setEvaluationResult(EvaluationResult result) {
        this.evaluationResult = result;
    }

    public String format() {
        var builder = new StringBuilder();

        var LUT = evaluationResult.getLUT();
        var extractorMap = evaluationResult.getExtractors();
        var synonyms = extractorMap.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList());

        var results = LUT.values()
                .stream()
                .map(set -> set.stream().collect(Collectors.toList()))
                .collect(Collectors.toList());

        var cartesianProduct = new CartesianProduct<ASTNode>();
        var product = cartesianProduct.product(results);


        for(int p = 0; p < product.size(); p++)
        {
            var list = product.get(p);

            for(int i = 0; i < list.size(); i++)
            {
                builder.append(
                        extractorMap.get(synonyms.get(i))
                                .apply(list.get(i))
                );

                if(i < list.size()-1)
                    builder.append(", ");
            }

            if(p < product.size()-1)
                builder.append(System.getProperty("line.separator"));
        }

        return builder.toString();
    }
}
