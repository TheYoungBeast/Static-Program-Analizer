package queryresultprojector;

import pkb.ast.abstraction.ASTNode;
import queryprocessor.evaluator.EvaluationResult;
import queryprocessor.evaluator.PartialResult;
import queryprocessor.preprocessor.Keyword;
import queryprocessor.preprocessor.Synonym;
import utils.Pair;

import java.util.*;

public class QueryResultProjector
{
    private EvaluationResult evaluationResult;

    public void setEvaluationResult(EvaluationResult result) {
        this.evaluationResult = result;
    }

    public String format() {
        if(evaluationResult == null) {
            return "None";
        }

        var builder = new StringBuilder();
        var extractorMap = evaluationResult.getExtractors();
        var synonyms = new ArrayList<>(extractorMap.keySet());

        var partialResults = evaluationResult.getPartialResults();
        var map  = new LinkedHashMap<Synonym<?>, PartialResult>();

        var relationshipsForKey = new HashMap<Synonym<?>, Pair<Synonym<?>, Set<Pair<ASTNode, ASTNode>>>>();

        for (var synonym: synonyms) {
            for (var pr: partialResults) {
                if(pr.containsKey(synonym))
                    map.putIfAbsent(synonym, pr);

                var pair = pr.getKeyPair();
                if(pair != null) {
                    var firstKey = pair.getFirst();
                    var secondKey = pair.getSecond();

                    relationshipsForKey.putIfAbsent(firstKey, new Pair<>(secondKey, (Set<Pair<ASTNode, ASTNode>>) pr.getValue()));
                }
            }
        }

        if(synonyms.stream().anyMatch(s -> s.getKeyword().equals(Keyword.BOOLEAN))) {
            builder.append("1 result(s): \n");

            var empty = partialResults.stream().anyMatch(pr -> pr.getValue().isEmpty()) || partialResults.isEmpty();
            if(empty)
                builder.append("False");
            else
                builder.append("True");


            return builder.toString();
        }

        var maxSize = 0;
        var combinations = partialResults.isEmpty() ? 0 : 1;
        for (var partialSet: partialResults) {
            boolean valid = false;
            for (var synonym: synonyms) {
                if(!partialSet.containsKey(synonym))
                    continue;

                valid = true;
                break;
            }

            if(!valid)
                continue;

            var set = partialSet.getValue();
            if(set.size() > maxSize)
                maxSize = set.size();

            combinations *= set.size();
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

        for (int i = 0; i < synonymsInRel.length; i++) {
            if(!synonymsInRel[i])
                continue;

            if(synonymsChecked[i])
                continue;

            if(!relationshipsForKey.containsKey(synonyms.get(i)))
                continue;

            var synonym = synonyms.get(i);
            var synonymPos = synonyms.indexOf(synonym);
            var value = relationshipsForKey.get(synonym);
            var secondSynonym = value.getFirst();
            var secondSynonymPos = synonyms.indexOf(secondSynonym);
            var allowedNodes = value.getSecond();

            if(secondSynonymPos == -1) {
                filtered2.addAll(results);
                break;
            }

            synonymsChecked[synonymPos] = true;
            synonymsChecked[secondSynonymPos] = true;

            for (var result: results) {
                var node1 = result.get(synonymPos);
                var node2 = result.get(secondSynonymPos);

                if(allowedNodes.contains(new Pair<>(node1, node2)))
                    filtered2.add(result);
            }
        }

        results = filtered2;

        //builder.append(String.format("%d result(s): \n", results.size()));

        var extractors = new ArrayList<>(extractorMap.values());
        for(int p = 0; p < results.size(); p++)
        {
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

            if(p < results.size()-1)
                builder.append(",");
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
