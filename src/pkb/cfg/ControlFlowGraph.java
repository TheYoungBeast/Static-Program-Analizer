package pkb.cfg;

import pkb.ast.IfNode;
import pkb.ast.ProcedureNode;
import pkb.ast.WhileNode;
import pkb.ast.abstraction.ASTNode;
import pkb.ast.abstraction.StatementNode;
import utils.Pair;

import java.util.*;


public class ControlFlowGraph
{
    private final CfgNode graph;

    public ControlFlowGraph(CfgNode node) {
        graph = node;
    }

    public static ControlFlowGraph build(ProcedureNode procedureNode)
    {
        var cfg = new CfgNode();
        generateCfg(procedureNode.getFirstChild(), cfg);

        return new ControlFlowGraph(cfg);
    }

    public List<List<CfgNode>> getFlowPaths(ASTNode from, ASTNode to)
    {
        var nodeFrom = seekNode(from);
        var nodeTo = seekNode(to);

        if(nodeFrom.isEmpty() || nodeTo.isEmpty())
            return Collections.emptyList();

        /*
        // simple workaround for cycles in graph
        if(nodeFrom.get() == nodeTo.get()) {
            var node = nodeFrom.get();
            nodeFrom = Optional.empty();

            if(node.getLeft() != null)
                nodeFrom = Optional.of(node.getLeft());
            else if (node.getRight() != null)
                nodeFrom = Optional.of(node.getRight());
        }



        if(nodeFrom.isEmpty())
            return Collections.emptyList();
         */

        var currentPath = new ArrayList<CfgNode>();
        var flowPaths = new ArrayList<List<CfgNode>>();
        var visited = new HashSet<CfgNode>();

        DFS(nodeFrom.get(),
                nodeTo.get(),
                visited,
                currentPath,
                flowPaths,
                nodeFrom.get().getAstNode().getStatementId() == nodeTo.get().getAstNode().getStatementId(),
                0);

        return flowPaths;
    }

    private void DFS(CfgNode u, CfgNode v, Set<CfgNode> visited, ArrayList<CfgNode> currentPath, ArrayList<List<CfgNode>> flowPaths, final boolean cycle, int count) {
        if(visited.contains(u))
            return;

        visited.add(u);
        currentPath.add(u);

        if(u.equals(v))
        {
            if(cycle) {
                count += 1;
                if(count < 2)
                    visited.remove(u);
            }

            if(count == 2 || !cycle) {
                flowPaths.add((List<CfgNode>) currentPath.clone());
                visited.remove(u);
                currentPath.remove(currentPath.size() - 1);
                return;
            }
        }

        if(u.getLeft() != null)
            DFS(u.getLeft(), v, visited, currentPath, flowPaths, cycle, count);

        if(u.getRight() != null)
            DFS(u.getRight(), v, visited, currentPath, flowPaths, cycle, count);

        if(!currentPath.isEmpty())
            currentPath.remove(currentPath.size()-1);

        visited.remove(u);
    }

    public Pair<CfgNode, CfgNode> getBranching(ASTNode astNode) {
        var cfgNode = seekNode(astNode);

        if(cfgNode.isPresent()) {
            var node = cfgNode.get();

            var left = shiftNode(node.getLeft());
            var right = shiftNode(node.getRight());

            return new Pair<>(left, right);
        }

        return new Pair<>(null, null);
    }

    private CfgNode shiftNode(CfgNode cfgNode)
    {
        Stack<CfgNode> cfgStack = new Stack<>();
        cfgStack.add(cfgNode);

        while (!cfgStack.isEmpty()) {
            var node = cfgStack.pop();

            if(node == null)
                continue;

            if(node instanceof EscapeBlock || node.getAstNode() == null) {
                cfgStack.add(node.getLeft());
                cfgStack.add(node.getRight());
                continue;
            }

            return node;
        }

        return cfgNode;
    }

    private Optional<CfgNode> seekNode(ASTNode astNode) {
        Stack<CfgNode> cfgStack = new Stack<>();
        cfgStack.add(graph);

        Set<CfgNode> visited = new HashSet<>();
        while (!cfgStack.isEmpty()) {
            var node = cfgStack.pop();

            if(node == null)
                continue;
            else if(node.getAstNode() != null && node.getAstNode().equals(astNode))
                return Optional.of(node);

            visited.add(node);

            if(!visited.contains(node.getLeft()))
                cfgStack.add(node.getLeft());

            if(!visited.contains(node.getRight()))
                cfgStack.add(node.getRight());
        }

        return Optional.empty();
    }

    private static CfgNode generateCfg(ASTNode head, CfgNode cfgHead)
    {
        CfgNode lastCfgNode = cfgHead;
        var aNode = head;

        while(aNode != null)
        {
            if (aNode instanceof WhileNode)
            {
                var whileCfgNode = new CfgNode();
                lastCfgNode.setLeft(whileCfgNode);

                whileCfgNode.setAstNode(aNode);

                var anchor = new CfgNode();
                var stmtList = aNode.getFirstChild().getRightSibling().getFirstChild();

                var lastStmt = generateCfg(stmtList, anchor);

                whileCfgNode.setLeft(anchor.getLeft());
                lastStmt.setLeft(whileCfgNode);

                var nextCfg = new EscapeBlock();
                whileCfgNode.setRight(nextCfg);

                lastCfgNode = nextCfg;
            }
            else if(aNode instanceof IfNode)
            {
                var thenBlock = aNode.getFirstChild().getRightSibling();    //    if
                var elseBlock = thenBlock.getRightSibling();            //    /   |   \
                                                                        //  var  then  else
                var ifNode = new CfgNode();
                ifNode.setAstNode(aNode);
                lastCfgNode.setLeft(ifNode);

                var endIfNode = new EscapeBlock();

                {
                    var anchor = new CfgNode();
                    var lastThenStmt = generateCfg(thenBlock.getFirstChild(), anchor);
                    ifNode.setLeft(anchor.getLeft());
                    lastThenStmt.setRight(endIfNode);
                }

                if(elseBlock != null) {
                    var anchor = new CfgNode();
                    var lastElseStmt = generateCfg(elseBlock.getFirstChild(), anchor);
                    ifNode.setRight(anchor.getLeft());
                    lastElseStmt.setLeft(endIfNode);
                }

                lastCfgNode = endIfNode;
            }
            else if(aNode instanceof StatementNode)
            {
                var cfg = new CfgNode();
                lastCfgNode.setLeft(cfg);

                cfg.setAstNode(aNode);
                lastCfgNode = cfg;
            }

            aNode = aNode.getRightSibling();
        }

        return lastCfgNode;
    }
}
