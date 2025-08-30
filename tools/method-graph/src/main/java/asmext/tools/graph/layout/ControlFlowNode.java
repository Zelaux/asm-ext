package asmext.tools.graph.layout;

import asmext.tools.graph.util.IntSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

    public NodeKind kind() {
        NodeKind kind = getNonMergedKind();
        return previous.size() < 2 ? kind : kind.merged();
    }
@NotNull
    private NodeKind getNonMergedKind() {
        if (simpleNext == -1 && gotoNext.isEmpty()) return NodeKind.End;
        if (simpleNext != -1 && gotoNext.isEmpty()) return NodeKind.Simple;
        if (simpleNext != -1 && gotoNext.isOne())
            return gotoNext.first() >= myIndex ? NodeKind.IfStmt : NodeKind.IfLoop;
        int size = gotoNext.size();
        if (simpleNext == -1 && size > 0) {
            if (size > 1) return NodeKind.Switch;
            return gotoNext.first() >= myIndex ? NodeKind.Goto : NodeKind.GotoLoop;
        }
        throw new UnsupportedOperationException();
    }
}
