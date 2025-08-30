package asmext.tools.graph.layout;

import asmext.tools.graph.util.IntSet;
import org.objectweb.asm.tree.AbstractInsnNode;

public class NodeGroup {
    final int straitNextIndex;
    final NodeKind kind;
    final ControlFlowNode[] nodes;
    final IntSet gotoNext;
    final IntSet previous;

    public NodeGroup(int straitNextIndex, NodeKind kind, ControlFlowNode[] nodes, IntSet gotoNext, IntSet previous) {
        this.straitNextIndex = straitNextIndex;
        this.kind = kind;
        this.nodes = nodes;
        this.gotoNext = gotoNext;
        this.previous = previous;
    }
}
