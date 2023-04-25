package queryresultprojector;

import pkb.ast.abstraction.ASTNode;
import queryprocessor.evaluator.EvaluationResult;
import queryprocessor.evaluator.PartialResult;
import queryprocessor.preprocessor.Keyword;
import queryprocessor.preprocessor.synonyms.Synonym;
import utils.Pair;

import java.util.*;

public class QueryResultProjector
{
    private final static String NoResultMsg = "none";
    private EvaluationResult evaluationResult;

    public void setEvaluationResult(EvaluationResult result) {
        this.evaluationResult = result;
    }

    public String format() {
        if(evaluationResult == null || evaluationResult.getPartialResults().isEmpty()) {
            return NoResultMsg;
        }

        var builder = new StringBuilder();
        var extractorMap = evaluationResult.getExtractors();
        var synonyms = new ArrayList<>(extractorMap.keySet());

        var partialResults = evaluationResult.getPartialResults();
        var map  = new LinkedHashMap<Synonym<?>, PartialResult>();

        var relationshipsForKey = new HashMap<Pair<Synonym<?>, Synonym<?>>, Set<Pair<ASTNode, ASTNode>>>();

        for (var synonym: synonyms) {
            for (var pr: partialResults) {
                if(pr.containsKey(synonym))
                    map.putIfAbsent(synonym, pr);

                var pair = pr.getKeyPair();
                if(pair != null) {
                    relationshipsForKey.putIfAbsent(pair, (Set<Pair<ASTNode, ASTNode>>) pr.getValue());
                }
            }
        }

        if(synonyms.stream().anyMatch(s -> s.getKeyword().equals(Keyword.BOOLEAN))) {
            var empty = partialResults.stream().anyMatch(pr -> pr.getValue().isEmpty()) || partialResults.isEmpty();
            if(empty)
                builder.append("false");
            else
                builder.append("true");

            return builder.toString();
        }

        List<List<ASTNode>> results = new ArrayList<>();

        List<List<ASTNode>> source = new ArrayList<>();
        for (var entry: map.entrySet()) {
            var values = new ArrayList<>(entry.getValue().getValue(entry.getKey()));
            source.add(values);
        }

        results = recursiveSetCombination(results, source, 0);

        results = new ArrayList<>(new HashSet<>(results));

        boolean[] synonymsInRel = new boolean[synonyms.size()];
        boolean[] synonymsChecked = new boolean[synonyms.size()];

        boolean relExists = false;
        for (int i = 0; i < synonyms.size(); i++) {
            var synonym = synonyms.get(i);
            if(relationshipsForKey.containsKey(synonym)) {
                synonymsInRel[i] = true;
                relExists = true;
            }
        }

        var filtered2 = relExists ? new ArrayList<List<ASTNode>>() : results;
        var resultValidity = new boolean[results.size()];

        for (int i = 0; i < results.size(); i++)
        {
            var result = results.get(i);
            var valid = 0;
            for (var entry : relationshipsForKey.entrySet()) {
                var pair = entry.getKey();

                var synonym = pair.getFirst();
                var synonymPos = synonyms.indexOf(synonym);
                var value = entry.getValue();
                var secondSynonym = pair.getSecond();
                var secondSynonymPos = synonyms.indexOf(secondSynonym);

               if(secondSynonymPos == -1 || synonymPos == -1) {
                    resultValidity[i] = true;
                    continue;
                }

                //synonymsChecked[synonymPos] = true;
                //synonymsChecked[secondSynonymPos] = true;

                var node1 = result.get(synonymPos);
                var node2 = result.get(secondSynonymPos);

                if (value.contains(new Pair<>(node1, node2)))
                    valid++;
            }

            if(valid == relationshipsForKey.keySet().size())
                resultValidity[i] = true;
        }

        results = filtered2;

        //builder.append(String.format("%d result(s): \n", results.size()));

        var extractors = new ArrayList<>(extractorMap.values());
        var printed = 0;
        for(int p = 0; p < results.size(); p++)
        {
            if(!resultValidity[p])
                continue;

            if(printed > 0)
                builder.append(", ");

            var list = results.get(p);

            for(int i = 0; i < list.size(); i++)
            {
                assert (extractors.size() == list.size());
                var extractor = extractors.get(i);
                var node = list.get(i);
                builder.append(extractor.apply(node));

                if(i < list.size()-1)
                    builder.append(" ");
            }

            printed++;
        }

        return builder.toString();
    }

    List<List<ASTNode>> recursiveSetCombination(List<List<ASTNode>> dest, List<List<ASTNode>> source, int step) {
        if(step == source.size())
            return dest;

        if(step == 0) {
            for(int i = 0; i < source.get(step).size(); i++)
                dest.add(List.of(source.get(step).get(i)));

            return recursiveSetCombination(dest, source, step+1);
        }
        else {
            var newResult = new ArrayList<List<ASTNode>>();

            for (var res: dest) {
                for (var s: source.get(step)) {
                    var newList = new ArrayList<ASTNode>(res.size());
                    newList.addAll(res);
                    newList.add(s);
                    newResult.add(newList);
                }
            }

            return recursiveSetCombination(newResult, source, step+1);
        }
    }
}
