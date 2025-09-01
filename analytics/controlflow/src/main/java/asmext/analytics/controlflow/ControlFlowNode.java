package asmext.analytics.controlflow;

import org.jetbrains.annotations.Debug;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * @author Zelaux
 */
@Debug.Renderer(text = "kind()+\" at \" + myIndex")
public class ControlFlowNode {
    public final InsnIdxSet previous = new InsnIdxSet(5);
    public final AbstractInsnNode node;
    public final int myIndex;
    public final InsnIdxSet gotoNext = new InsnIdxSet(5);
    public int simpleNext = -1;

    public ControlFlowNode(AbstractInsnNode node, int myIndex) {
        this.node = node;
        this.myIndex = myIndex;
    }

    public boolean isDead() {
        return simpleNext == -1 && previous.isEmpty() && gotoNext.isEmpty();
    }

    public ConnectionType kind() {
        var kind = getOutputType();
        return kind.connectionType(previous.size() > 1);
    }

    @NotNull
    private OutputType getOutputType() {
        return OutputType.getNodeKind(simpleNext != -1, gotoNext, myIndex);
    }

}
