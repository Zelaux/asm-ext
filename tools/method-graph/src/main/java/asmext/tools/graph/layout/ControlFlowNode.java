package asmext.tools.graph.layout;

import asmext.tools.graph.util.IntSet;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.AbstractInsnNode;

public class ControlFlowNode {
    public final IntSet previous = new IntSet(5);
    public final AbstractInsnNode node;
    public final int myIndex;
    public final IntSet gotoNext = new IntSet(5);
    public int simpleNext = -1;

    public ControlFlowNode(AbstractInsnNode node, int myIndex) {
        this.node = node;
        this.myIndex = myIndex;
    }

    public FullNodeKind kind() {
        var kind = getNonMergedKind();
        return previous.size() < 2 ? kind.fullType() : kind.merged();
    }

    @NotNull
    private NodeKind getNonMergedKind() {
        return NodeKind.getNodeKind(simpleNext != -1, gotoNext, myIndex);
    }

}
