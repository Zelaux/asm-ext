package asmext.analytics.controlflow;

import org.jetbrains.annotations.Debug;
import org.jetbrains.annotations.NotNull;

/**
 * @author Zelaux
 */
@Debug.Renderer(text = "debugString()")
public class ControlFlowBlock {
    public final int nextNodeIndex;
    public final ConnectionType kind;
    public final ControlFlowNode first;
    public final ControlFlowNode[] nodes;
    public final ControlFlowNode last;
    public final InsnIdxSet gotoNext;
    public final InsnIdxSet previous;
    public String debugString(){
        String rangePart = "range [" + first.myIndex + "; " + last.myIndex + "]";
        if(isDead())return rangePart+" dead";
        return rangePart+" kind "+kind;
    }

    public ControlFlowBlock(int nextNodeIndex, ControlFlowNode[] nodes, InsnIdxSet gotoNext, InsnIdxSet previous) {
        this(nextNodeIndex, kind(nextNodeIndex, nodes, gotoNext, previous), nodes, gotoNext, previous);
    }

    public ControlFlowBlock(int nextNodeIndex, ConnectionType kind, ControlFlowNode[] nodes, InsnIdxSet gotoNext, InsnIdxSet previous) {
        this.nextNodeIndex = nextNodeIndex;
        this.kind = kind;
        this.nodes = nodes;
        first = nodes[0];
        last = nodes[nodes.length - 1];
        this.gotoNext = gotoNext;
        this.previous = previous;
//        if (kind.isMerge() || kind.outputType() != OutputType.Simple) return;
//        throw new IllegalArgumentException("illegal block structure");
        //ifStmt with one branch

    }

    public ControlFlowBlock(ControlFlowNode[] groupNodes) {
        this(
                groupNodes[groupNodes.length - 1].simpleNext,
                groupNodes,
                groupNodes[groupNodes.length - 1].gotoNext.copy(),
                groupNodes[0].previous.copy()
        );
    }

    private static @NotNull ConnectionType kind(int straitNextIndex, ControlFlowNode[] nodes, InsnIdxSet gotoNext, InsnIdxSet previous) {
        OutputType outputType = OutputType.getNodeKind(straitNextIndex != -1, gotoNext, nodes[nodes.length - 1].myIndex);
        return outputType.connectionType(previous.size() > 1);
    }

    public static ControlFlowBlock deadBlock(ControlFlowNode[] flowNodes) {
        return new ControlFlowBlock(flowNodes);
    }

    public boolean isDead() {
        return first.isDead();
    }
}
