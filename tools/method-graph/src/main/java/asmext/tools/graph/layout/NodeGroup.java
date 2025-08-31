package asmext.tools.graph.layout;

import asmext.tools.graph.util.IntSet;
import org.jetbrains.annotations.NotNull;

public class NodeGroup {
    final int straitNextIndex;
    final FullNodeKind kind;
    final ControlFlowNode first;
    final ControlFlowNode[] nodes;
    final ControlFlowNode last;
    final IntSet gotoNext;
    final IntSet previous;

    public NodeGroup(int straitNextIndex, ControlFlowNode[] nodes, IntSet gotoNext, IntSet previous) {
        this(straitNextIndex, kind(straitNextIndex, nodes, gotoNext, previous), nodes, gotoNext, previous);
    }

    public NodeGroup(int straitNextIndex, FullNodeKind kind, ControlFlowNode[] nodes, IntSet gotoNext, IntSet previous) {
        this.straitNextIndex = straitNextIndex;
        this.kind = kind;
        this.nodes = nodes;
        first = nodes[0];
        last = nodes[nodes.length - 1];
        this.gotoNext = gotoNext;
        this.previous = previous;

    }

    private static @NotNull FullNodeKind kind(int straitNextIndex, ControlFlowNode[] nodes, IntSet gotoNext, IntSet previous) {
        NodeKind nodeKind = NodeKind.getNodeKind(straitNextIndex != -1, gotoNext, nodes[nodes.length - 1].myIndex);
        return previous.size() <= 1 ? nodeKind.fullType() : nodeKind.merged();
    }
}
