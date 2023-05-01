package queryprocessor.evaluator;

import pkb.ast.abstraction.ASTNode;
import pkb.ProgramKnowledgeBaseAPI;
import queryprocessor.evaluator.abstraction.EvaluationEngine;
import queryprocessor.evaluator.abstraction.QueryEvaluator;
import queryprocessor.preprocessor.Keyword;
import queryprocessor.preprocessor.synonyms.Synonym;
import queryprocessor.querytree.*;
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
        evalAlgorithms.put(Keyword.CALLS, engine::evaluateCallsRel);
        evalAlgorithms.put(Keyword.T_CALLS, engine::evaluateCallsRel);
        evalAlgorithms.put(Keyword.FOLLOWS, engine::evaluateFollowsRel);
        evalAlgorithms.put(Keyword.T_FOLLOWS, engine::evaluateFollowsTransitiveRel);
        evalAlgorithms.put(Keyword.AFFECTS, engine::evaluateAffectRel);
        evalAlgorithms.put(Keyword.T_AFFECTS, engine::evaluateAffectRel);
        evalAlgorithms.put(Keyword.NEXT, engine::evaluateNextRel);
        evalAlgorithms.put(Keyword.T_NEXT, engine::evaluateNextTransitiveRel);
    }

    @Override
    public EvaluationResult evaluate(QueryTree queryTree)
    {
        var resultNodes = new ArrayList<ResNode>();

        if(queryTree == null)
            return null;

        var rNode = queryTree.getResultsNode().getFirstChild();

        // LinkedHashMap
        // in addition to the uniqueness of elements, the order of elements in which they were added is also guaranteed
        // important, the rest of the function relies on this order
        var resultLUT = new LinkedHashMap<Synonym<?>, Set<ASTNode>>();
        var resultExtractors = new LinkedHashMap<Synonym<?>, Function<ASTNode, String>>();

        while(rNode != null)
        {
            if(rNode instanceof ResNode)
                resultNodes.add((ResNode) rNode);

            rNode = rNode.getRightSibling();
        }

        for (var resNode: resultNodes) {
            var s = resNode.getSynonym();
            resultLUT.computeIfAbsent(s, l -> this.getMatchingNodes(pkb.getAST(), s));
            resultExtractors.computeIfAbsent(s, e -> resNode.getExtractor());
        }

        var refRefConditions = new HashMap<Pair<Synonym<?>, Synonym<?>>, LinkedHashSet<Pair<ASTNode, ASTNode>>>();
        if(queryTree.getWithNode() != null)
        {
            var conditionsPair = new ArrayList<Pair<Condition, Boolean>>();
            var condNode = (Condition) queryTree.getWithNode().getFirstChild();

            while (condNode != null) {
                if(condNode instanceof ConditionRefRef)
                    conditionsPair.add(new Pair<>(condNode, true)); // conditions such as x.procName = y.procName
                else
                    conditionsPair.add(new Pair<>(condNode, false)); // conditions such as x.procName = "Procedure"

                condNode = (Condition) condNode.getRightSibling();
            }

            var doubleRefConditions = conditionsPair.stream().filter(Pair::getSecond).map(p -> (ConditionRefRef) p.getFirst()).collect(Collectors.toList());
            var refValueConditions = conditionsPair.stream().filter(p -> !p.getSecond()).map(p -> (ConditionRefValue) p.getFirst()).collect(Collectors.toList());

            for (var condition: refValueConditions) {
                var wResults = resultLUT.computeIfAbsent(condition.getAttrRef().getSynonym(),
                        l -> getMatchingNodes(pkb.getAST(), condition.getAttrRef().getSynonym()));

                var cResult = wResults.stream()
                        .filter(condition::attrCompare)
                        .collect(Collectors.toList());

                resultLUT.put(condition.getAttrRef().getSynonym(), new HashSet<>(cResult));
            }

            for (var condition: doubleRefConditions) {
                var pair = condition.getAttrRefs();
                var ref1 = pair.getFirst();
                var ref2 = pair.getSecond();

                var ref1Nodes = resultLUT.computeIfAbsent(ref1.getSynonym(), l -> getMatchingNodes(pkb.getAST(), ref1.getSynonym()));
                var ref2Nodes = resultLUT.computeIfAbsent(ref2.getSynonym(), l -> getMatchingNodes(pkb.getAST(), ref2.getSynonym()));

                Set<ASTNode> filteredRefs1 = new HashSet<>();
                Set<ASTNode> filteredRefs2 = new HashSet<>();
                for (var ref1Node: ref1Nodes) {
                    for (var ref2Node: ref2Nodes) {
                        var refPair = new Pair<>(ref1Node, ref2Node);
                        if(condition.attrCompare(refPair)) {
                            filteredRefs1.add(ref1Node);
                            filteredRefs2.add(ref2Node);
                            refRefConditions.computeIfAbsent(new Pair<>(ref1.getSynonym(), ref2.getSynonym()), l -> new LinkedHashSet<>()).add(refPair);
                        }
                    }
                }

                resultLUT.put(ref1.getSynonym(), filteredRefs1);
                resultLUT.put(ref2.getSynonym(), filteredRefs2);
            }
        }

        var patterns = new ArrayList<ExpressionPattern>();
        if(queryTree.getPatternNode() != null){
            ExpressionPattern node = (ExpressionPattern) queryTree.getPatternNode().getFirstChild();

            while (node != null) {
                patterns.add(node);
                node = (ExpressionPattern) node.getRightSibling();
            }

            for (var pattern: patterns) {
                var statements = resultLUT.computeIfAbsent(pattern.getSynonym(), l -> getMatchingNodes(pkb.getAST(), pattern.getSynonym()));
                var result = new HashSet<ASTNode>();

                for (var stmt: statements) {
                    if(pattern.matchesPattern(stmt))
                        result.add(stmt);
                }

                resultLUT.put(pattern.getSynonym(), result);
            }
        }

        List<PartialResult> partialResults = new ArrayList<>();
        Map<Pair<Synonym<?>, Synonym<?>>, LinkedHashSet<Pair<ASTNode, ASTNode>>> pairsInRelationshipMap = new HashMap<>();
        if(queryTree.getSuchThatNode() != null)
        {
            var relNode = queryTree.getSuchThatNode().getFirstChild();
            var relationships = new ArrayList<RelationshipRef>();

            while (relNode != null) {
                if(relNode instanceof RelationshipRef)
                    relationships.add((RelationshipRef) relNode);

                relNode = relNode.getRightSibling();
            }

            // Sortowanie malejące relacji względem ich priorytetu określającego nakład obliczeniowy
            // Relacje, które są łatwe i szybkie do obliczenia mają najwyższy priorytet i są obliczane jako pierwsze
            // Ograniczając tym pulę kandydatów dla których mogą zajść kolejne relacje
            // Zmiejsza to czas potrzebny na obliczenie kolejnych, "cięższych" relacji
            relationships.sort(Comparator.comparing(r -> r.getComputingPriority().getPriority(), Comparator.reverseOrder()));

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

        // Stworz rezultaty cząstkowe dla wyników warunków gdzie występuje porównanie pomiędzy dwoma referencjami synonimu
        // Klucz podwójnu - <Synonim pierwszej referencji, Synonim drugiej referencji>
        // Zestaw zawiera pary dla których spełniony jest warunek (przykład: x1.procName = y2.procName);
        for (var entry: refRefConditions.entrySet()) {
            var keyPair = entry.getKey();
            var valueSet = entry.getValue();

            PartialResult pr = new PartialResult(keyPair, valueSet);
            partialResults.add(pr);
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

            if(!contains && !entry.getValue().isEmpty())
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
