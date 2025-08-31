package asmext.tools.graph.layout;

import asmext.tools.graph.util.Utils;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;

import java.util.List;

public class ControlFlowAnalyzer extends Analyzer<BasicValue> {

    private static final InsnList mockList = new InsnList();
    private AbstractInsnNode[] instructionsArray;
    private ControlFlowNode[] nodes;

    /**
     * Constructs a new {@link Analyzer}.
     *
     * @param interpreter the interpreter to use to symbolically interpret the bytecode instructions.
     */
    public ControlFlowAnalyzer() {
        super(new BasicInterpreter());
    }

    @Override
    public Frame<BasicValue>[] analyze(String owner, MethodNode method) throws AnalyzerException {
        instructionsArray = method.instructions.toArray();
        nodes = new ControlFlowNode[instructionsArray.length];
        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = new ControlFlowNode(instructionsArray[i], i);
        }
        Frame<BasicValue>[] analyze = super.analyze(owner, method);
        return analyze;
    }

    public ControlFlowNode[] getNodes() {
        return nodes;
    }

    @Override
    protected Frame<BasicValue> newFrame(int numLocals, int numStack) {
        return super.newFrame(numLocals, numStack);
    }

    @Override
    protected Frame<BasicValue> newFrame(Frame<? extends BasicValue> frame) {
        return super.newFrame(frame);
    }

    @Override
    protected void newControlFlowEdge(int insnIndex, int successorIndex) {
        ControlFlowNode curNode = nodes[insnIndex];
        ControlFlowNode nextNode = nodes[successorIndex];
        setIndex(insnIndex, successorIndex, nextNode, curNode);
        super.newControlFlowEdge(insnIndex, successorIndex);
    }

    private void setIndex(int insnIndex, int successorIndex, ControlFlowNode nextNode, ControlFlowNode curNode) {
        nextNode.previous.add(insnIndex);
        AbstractInsnNode insnNode = instructionsArray[insnIndex];
        if (insnNode instanceof JumpInsnNode jumpInsnNode) {
            if (mockList.indexOf(jumpInsnNode.label) == successorIndex) {
                curNode.gotoNext.add(successorIndex);
                return;
            }
        } else {
            List<LabelNode> switchLabel = Utils.getSwitchLabels(insnNode);
            if (switchLabel != null) {
                for (LabelNode node : switchLabel) {
                    if (mockList.indexOf(node) == successorIndex) {
                        curNode.gotoNext.add(successorIndex);
                        return;
                    }
                }
            }
        }
        curNode.simpleNext = successorIndex;
    }

    public static class MyFrame extends Frame<BasicValue> {

        public MyFrame(int numLocals, int maxStack) {
            super(numLocals, maxStack);
        }

        public MyFrame(Frame<? extends BasicValue> frame) {
            super(frame);
        }

    }
}
