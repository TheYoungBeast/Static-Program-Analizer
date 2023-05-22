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

            if(node instanceof EndIfNode || node.getAstNode() == null) {
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
            if (aNode instanceof WhileNode) {
                var whileNode = new CfgNode();
                lastCfgNode.setLeft(whileNode);
                whileNode.setAstNode(aNode);
                var stmtList = aNode.getFirstChild().getRightSibling(); // WhileNode
                                                                        //      |
                                                                        //   condition ——> stmtList
                whileNode.setLeft(new CfgNode());
                lastCfgNode = generateCfg(stmtList.getFirstChild(), whileNode);
                lastCfgNode.setLeft(whileNode);
                whileNode.setRight(new CfgNode());
                whileNode.getRight().setAstNode(aNode.getRightSibling());
                aNode = aNode.getRightSibling();
                lastCfgNode = whileNode.getRight();
            }
            else if(aNode instanceof IfNode) {
                var then = aNode.getFirstChild().getRightSibling(); //   if
                var elsee = then.getRightSibling();             //    /  |  \
                                                                //  var  then  else
                var ifNode = new CfgNode();
                ifNode.setAstNode(aNode);
                lastCfgNode.setLeft(ifNode);

                var thenNode = new CfgNode();
                thenNode.setAstNode(then.getFirstChild());
                ifNode.setLeft(thenNode);
                var lastThenStmt = generateCfg(then.getFirstChild(), thenNode);

                var endIfNode = new EndIfNode();
                lastThenStmt.setRight(endIfNode);

                if(elsee != null) {
                    var elseNode = new CfgNode();
                    elseNode.setAstNode(elsee.getFirstChild());
                    ifNode.setRight(elseNode);
                    var lastElseStmt = generateCfg(elsee.getFirstChild(), elseNode);
                    lastElseStmt.setLeft(endIfNode);
                }

                lastCfgNode = endIfNode;
            }
            else if(aNode instanceof StatementNode) {
                var cfg = new CfgNode();
                lastCfgNode.setLeft(cfg);
                cfg.setAstNode(aNode);
                lastCfgNode = cfg;
            }

            if(aNode != null)
                aNode = aNode.getRightSibling();
        }

        return lastCfgNode;
    }
}
