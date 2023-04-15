package queryprocessor.evaluator;

import pkb.ast.abstraction.ASTNode;
import pkb.ProgramKnowledgeBaseAPI;
import queryprocessor.evaluator.abstraction.EvaluationEngine;
import queryprocessor.evaluator.abstraction.QueryEvaluator;
import queryprocessor.preprocessor.Keyword;
import queryprocessor.preprocessor.Synonym;
import queryprocessor.querytree.ConditionNode;
import queryprocessor.querytree.QueryTree;
import queryprocessor.querytree.RelationshipRef;
import queryprocessor.querytree.ResNode;
import utils.Pair;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class QueryEvaluatorBase implements QueryEvaluator
{
    public final ProgramKnowledgeBaseAPI pkb;

    public final HashMap<Keyword, BiFunction<
            Set<ASTNode>,                  // arg1
            Set<ASTNode>,                  // arg2
            Set<Pair<ASTNode, ASTNode>>>   // result
            > evalAlgorithms = new HashMap<>();

    public QueryEvaluatorBase(ProgramKnowledgeBaseAPI pkb, EvaluationEngine engine) {
        this.pkb = pkb;

        evalAlgorithms.put(Keyword.PARENT, engine::evaluateParentRel);
        evalAlgorithms.put(Keyword.T_PARENT, engine::evaluateParentTransitiveRel);
        evalAlgorithms.put(Keyword.USES, engine::evaluateUsesRel);
        evalAlgorithms.put(Keyword.MODIFIES, engine::evaluateModifiesRel);
    }

    @Override
    public EvaluationResult evaluate(QueryTree queryTree)
    {
        var resultNodes = new ArrayList<ResNode>();
        var node = queryTree.getResultsNode().getFirstChild();

        // LinkedHashMap
        // in addition to the uniqueness of elements, the order of elements in which they were added is also guaranteed
        // important, the rest of the function relies on this order
        var resultLUT = new LinkedHashMap<Synonym<?>, Set<ASTNode>>();
        var resultExtractors = new LinkedHashMap<Synonym<?>, Function<ASTNode, String>>();

        while(node != null)
        {
            if(node instanceof ResNode)
                resultNodes.add((ResNode) node);

            node = node.getRightSibling();
        }

        for (var resNode: resultNodes) {
            var s = resNode.getSynonym();
            resultLUT.computeIfAbsent(s, l -> this.getMatchingNodes(pkb.getAST(), s));
            resultExtractors.computeIfAbsent(s, e -> resNode.getExtractor());
        }

        if(queryTree.getWithNode() != null)
        {
            var conditions = new ArrayList<ConditionNode>();
            var condNode =  queryTree.getWithNode().getFirstChild();

            while (condNode != null) {
                if(condNode instanceof ConditionNode)
                    conditions.add((ConditionNode) condNode);

                condNode = condNode.getRightSibling();
            }

            for (var condition: conditions) {
                var wResults = resultLUT.computeIfAbsent(condition.getAttrRef().getSynonym(),
                        l -> getMatchingNodes(pkb.getAST(), condition.getAttrRef().getSynonym()));

                var cResult = wResults.stream()
                        .filter(condition::attrCompare)
                        .collect(Collectors.toList());

                resultLUT.put(condition.getAttrRef().getSynonym(), new HashSet<>(cResult));
            }
        }

        List<PartialResult> partialResults = new ArrayList<>();
        Map<Pair<Synonym<?>, Synonym<?>>, LinkedHashSet<Pair<ASTNode, ASTNode>>> pairsInRelationshipMap = new HashMap<>();
        if(queryTree.getSuchThatNode() != null)
        {
            var relNode = queryTree.getSuchThatNode().getFirstChild();
            var relationships = new ArrayList<RelationshipRef>();

            while (relNode != null){
                if(relNode instanceof RelationshipRef)
                    relationships.add((RelationshipRef) relNode);

                relNode = relNode.getRightSibling();
            }

            // Dla każdej relacji załaduj do Look Up Table (LUT) opowiadające typy węzłów.
            // Każdy synonim zawiera w sobie opowiadający mu typ węzła drzewa AST oraz posiada komparator
            for (var relRef: relationships) {
                for (int i = 0; i < relRef.getArgSize(); i++) {
                    var arg = relRef.getArg(i);
                    resultLUT.computeIfAbsent(arg.getSynonym(), l -> getMatchingNodes(pkb.getAST(), arg.getSynonym()));
                }

                // Ekstrakcja argumentów relacji
                // Każda z relacji ma zawsze 2 parametry, raczej nigdy się to nie zmieni
                var arg1 = relRef.getArg(0);
                var arg2 = relRef.getArg(1);

                // evalAlgoritms zawiera mape algorytmów ewaluacyjnych w formie <K, V>
                // gdzie K to synonim, a V to 'instance method reference'
                // rezultatem jest lista par węzłów między którymi zachodzi dana relacja
                var results = new ArrayList<>(evalAlgorithms.get(relRef.getRelationshipType()).apply(
                        resultLUT.get(arg1.getSynonym()),
                        resultLUT.get(arg2.getSynonym())
                ));

                var firstSet = new HashSet<ASTNode>();
                var secondSet = new HashSet<ASTNode>();
                boolean anyMatch = false;
                for (var resultPair: results) {
                    var firstNode = resultPair.getFirst(); // Lista węzłów dla argumentu 1 danej relacji
                    var secondNode = resultPair.getSecond(); // Lista węzłów dla argumentu 2 danej relacji
                    var firstSynonym = arg1.getSynonym();

                    // sprawdź czy dany węzeł istnieje w LUT
                    // jeśli węzęł nie zawiera się w zestawie to znaczy, że dany wynik nie spełnia innych relacji
                    // innymi słowy, taki wynik nie należy do zbioru wspólnego (część wspólna) relacji
                    // wynik należy odrzucić
                    if(resultLUT.get(firstSynonym).stream().anyMatch(x -> x == firstNode)) {
                        var secondSynonym = arg2.getSynonym();

                        // analogicznie
                        // sprawdzenie - tym razem dla węzła pochodzącego z drugiego argumentu
                        if(resultLUT.get(secondSynonym).stream().anyMatch(x -> x == secondNode)) {
                            // jesli oba węzły należą do zbioru
                            // nalezy dodać taki wynik do listy par relacji
                            var set = pairsInRelationshipMap.computeIfAbsent(new Pair<>(firstSynonym, secondSynonym), l -> new LinkedHashSet<>());
                            set.add(resultPair);
                            firstSet.add(firstNode);
                            secondSet.add(secondNode);
                            anyMatch = true;
                        }
                    }
                }

                // jeśli żadna z par nie należy do zbioru to znaczy, że dalsze relacje nie mogą zostać
                // spełnione, ponieważ część wspólna tych relacji jest zbiorem pustym: pair<węzeł, węzeł> ∈ ∅
                if(!anyMatch) { // no results where matched, from this point no further relationship can hold
                    pairsInRelationshipMap.clear();
                    resultLUT.clear();
                    break;
                }

                // Zaktualizuj LUT węzłów
                // LUT zawiera aktualnie tylko węzły należące do części wspólnej wszystkich relacji
                resultLUT.put(arg1.getSynonym(), firstSet);
                resultLUT.put(arg2.getSynonym(), secondSet);
            }

            // Zaktualizuj mape relacji
            // Wyeliminuj stare relacje które zachodziły na początku, a potem zostały wyeliminowane przez dalsze relacji
            // i ich wynik nie należy do części wspólnej
            for (var entry: pairsInRelationshipMap.entrySet()) {
                var keyPair = entry.getKey();
                var pairSet = entry.getValue();

                var filteredSet = pairSet
                        .stream()
                        .filter(p ->
                                resultLUT.get(keyPair.getFirst()).contains(p.getFirst())
                                        &&
                                        resultLUT.get(keyPair.getSecond()).contains(p.getSecond())).
                        collect(Collectors.toCollection(LinkedHashSet::new));

                pairsInRelationshipMap.put(keyPair, filteredSet);
            }

            // Stworz rezultaty cząstkowe dla wyników relacji
            // PartialRezult moze zawierać jeden klucz prosty lub pare kluczy
            // tutaj parą kluczy są synonimy relacji, a zestawem argumenty dla których zachodzi dana relacja
            for (var entry: pairsInRelationshipMap.entrySet()) {
                var keyPair = entry.getKey();
                var valueList = entry.getValue();

                PartialResult pr = new PartialResult(keyPair, valueList);
                partialResults.add(pr);
            }
        }

        // Stworz rezultaty cząstkowe dla synonimow, dla których nie została zdefiniowana żadna relacja
        // klucz pojedyczny - Synonim
        // zestaw zawiera listę argumentów (węzłów) dla danego synonimu, który spełnia warunki o ile jakieś zostały zdefiniowane
        // w przeciwnym razie zawiera wszystkie odpowiadające mu typem argumenty (węzły)
        for (var entry: resultLUT.entrySet()) {
            var contains = false;

            for (var pr: partialResults) {
                if(pr.containsKey(entry.getKey())) {
                    contains = true;
                    break;
                }
            }

            if(!contains)
                partialResults.add(new PartialResult(entry.getKey(), entry.getValue()));
        }

        return new EvaluationResult(resultExtractors, partialResults);
    }

    private Set<ASTNode> getMatchingNodes(ASTNode head, Synonym<?> s) {
        var result = new ArrayList<ASTNode>();
        ASTNode node = head;

        Stack<ASTNode> nodeStack = new Stack<>();
        do {
            if(node == null) {
                if(!nodeStack.empty())
                    node = nodeStack.pop();
                continue;
            }
            nodeStack.add(node.getRightSibling());

            if(s.isDerivative(node))
                result.add(node);

            node = node.getFirstChild();
        } while(!nodeStack.empty() || node != null);

        return new HashSet<>(result);
    }
}
