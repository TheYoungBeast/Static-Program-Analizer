package frontend.designextractor;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import pkb.ProgramKnowledgeBase;
import pkb.ast.CallNode;
import pkb.ast.ProcedureNode;
import pkb.ast.ProgramNode;
import pkb.ast.VariableNode;
import pkb.ast.abstraction.ASTNode;


public class DesignExtractor {

    private static ProgramKnowledgeBase pkb;

    public static void extract(ProgramKnowledgeBase pkb) {
        DesignExtractor.pkb = pkb;
        calculateCallsRelations();
        calculateModifiesAndUsesForProcedure();
    }

    private static void calculateCallsRelations()
    {
        for (ProcedureNode procedure : pkb.getProcTable())
        {
            Stack<ASTNode> nodeStack = new Stack<>();
            ASTNode node = procedure.getFirstChild();
            do {
                if(node == null) {
                    if(!nodeStack.empty())
                        node = nodeStack.pop();
                    continue;
                }
                nodeStack.add(node.getRightSibling());

                if(node instanceof CallNode)
                    pkb.getCalls().computeIfAbsent(procedure, c -> new LinkedHashSet<>())
                            .add(((CallNode) node).getCalledProcedure());

                node = node.getFirstChild();
            } while(!nodeStack.empty() || node != null);
        }
    }

    public static void resolveDependencies()
    {
        var resolved = new HashSet<ProcedureNode>();
        var comparator = Comparator.comparingInt((ProcedureNode p) -> pkb.getCalls(p).size());

        var procedures = pkb.getProcTable();
        var proceduresToResolve = procedures.stream().sorted(comparator).collect(Collectors.toCollection(ArrayDeque::new)); // dequeue

        while(!proceduresToResolve.isEmpty())
        {
            var procedure = proceduresToResolve.poll(); // retrive and remove peak
            if(resolved.contains(procedure))
                continue;

            var calls = pkb.getCalls(procedure);

            if(calls.isEmpty() || resolved.containsAll(calls)) {
                resolved.add(procedure);
                for (var calledProcedure: calls) {
                    var mod = pkb.getModifies(calledProcedure);
                    var uses = pkb.getUses(calledProcedure);
                    pkb.addModifies(procedure, mod);
                    pkb.addUses(procedure, uses);
                }
            }

            if(!resolved.contains(procedure))
                proceduresToResolve.addLast(procedure); // push to the back to resolve it later
        }
    }

    private static void calculateModifiesAndUsesForProcedure()
    {
        for (ProcedureNode procedure : pkb.getProcTable()) {
            extractRelations(procedure, pkb.getModifies(), pkb::addModifies);
            extractRelations(procedure, pkb.getUses(), pkb::addUses);
        }

        resolveDependencies(); // CRUCIAL!!

        for (ASTNode node : pkb.getModifies().keySet()) {
            if (node instanceof CallNode) {
                pkb.addModifies(node, pkb.getModifies(((CallNode) node).getCalledProcedure()));
                ASTNode parent = node.getParent();
                do {
                    pkb.addModifies(parent, pkb.getModifies(node));
                    parent = parent.getParent();
                } while (!(parent instanceof ProgramNode));
            }
        }

        for (ASTNode node : pkb.getUses().keySet()) {
            if (node instanceof CallNode) {
                pkb.addUses(node, pkb.getUses(((CallNode) node).getCalledProcedure()));
                ASTNode parent = node.getParent();
                do {
                    pkb.addUses(parent, pkb.getUses(node));
                    parent = parent.getParent();
                } while (!(parent instanceof ProgramNode));
            }
        }
    }

    private static void extractRelations(
            ProcedureNode procedure,
            Map<ASTNode, Set<VariableNode>> relation,
            BiConsumer<ProcedureNode, Set<VariableNode>> add)
    {
        for (ASTNode node : relation.keySet()) {
            if (procedure.getStatements().contains(node)) {
                add.accept(procedure, relation.get(node));
            }
        }
    }
}
