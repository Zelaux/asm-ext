package asmext.analytics.controlflow;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.SourceInterpreter;
import org.objectweb.asm.tree.analysis.SourceValue;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Zelaux
 */
public class ControlFlowAnalyzer {

    private static final InsnList mockList = new InsnList();
    private final InnerControlFlowAnalyzer analyzer = new InnerControlFlowAnalyzer(this);
    private AbstractInsnNode[] instructionsArray;
    private ControlFlowNode[] nodes;

    public ControlFlowAnalyzer() {
        super();
    }

    private static List<LabelNode> getSwitchLabels(AbstractInsnNode insnNode) {
        if (insnNode instanceof LookupSwitchInsnNode lookupSwitch) {
            ArrayList<LabelNode> list = new ArrayList<>(lookupSwitch.labels);
            list.add(lookupSwitch.dflt);
            return list;
        }
        if (insnNode instanceof TableSwitchInsnNode tableSwitch) {
            ArrayList<LabelNode> list = new ArrayList<>(tableSwitch.labels);
            list.add(tableSwitch.dflt);
            return list;
        }
        return null;
    }

    public static @NotNull ArrayList<ControlFlowBlock> makeGroups(ControlFlowNode[] nodes) {
        ArrayList<ControlFlowBlock> groups = new ArrayList<>();
        {
            int prevStart = 0;
            for (int i = 0; i < nodes.length; i++) {
                ControlFlowNode node = nodes[i];

                ConnectionType kind = node.kind();
                if (kind == ConnectionType.Simple || !node.isDead() && prevStart==i) continue;
                int endIndex = node.isDead() ?
                        collectDeadGroup(nodes, i, prevStart, groups)
                        : collectNormalGroup(nodes, kind, i, prevStart, groups);
                prevStart = endIndex;
                i = endIndex - 1;

            }
        }
        return groups;
    }

    private static int collectNormalGroup(ControlFlowNode[] nodes, ConnectionType kind, int i, int prevStart, ArrayList<ControlFlowBlock> groups) {
        int endIndex = !kind.isMerge() ? i + 1 : i;

        ControlFlowNode[] groupNodes = new ControlFlowNode[endIndex - prevStart];

        System.arraycopy(nodes, prevStart, groupNodes, 0, groupNodes.length);

        groups.add(new ControlFlowBlock(groupNodes));
        return endIndex;
    }

    private static int collectDeadGroup(ControlFlowNode[] nodes, int i, int prevStart, ArrayList<ControlFlowBlock> groups) {
        while (i < nodes.length && nodes[i].isDead()) i++;

        ControlFlowNode[] flowNodes = new ControlFlowNode[i - prevStart];
        System.arraycopy(nodes, prevStart, flowNodes, 0, flowNodes.length);

        groups.add(ControlFlowBlock.deadBlock(flowNodes));
        return i;
    }

    public ControlFlowNode[] analyze(String owner, MethodNode method) throws AnalyzerException {
        instructionsArray = method.instructions.toArray();
        nodes = new ControlFlowNode[instructionsArray.length];
        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = new ControlFlowNode(instructionsArray[i], i);
        }
        this.analyzer.analyze(owner, method);
        return nodes;
    }

    public ArrayList<ControlFlowBlock> collectGroups() {
        return makeGroups(nodes);
    }


    private void handleControlFlowExceptionEdge(int insnIndex, int successorIndex) {
//        ControlFlowNode curNode = nodes[insnIndex];
//        ControlFlowNode nextNode = nodes[successorIndex];
//        setIndex(insnIndex, successorIndex, nextNode, curNode);
    }

    private void handleControlFlowEdge(int insnIndex, int successorIndex) {
        ControlFlowNode curNode = nodes[insnIndex];
        ControlFlowNode nextNode = nodes[successorIndex];
        setIndex(insnIndex, successorIndex, nextNode, curNode);
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
            List<LabelNode> switchLabel = getSwitchLabels(insnNode);
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

    static class InnerControlFlowAnalyzer extends Analyzer<SourceValue> {
        private final ControlFlowAnalyzer controlFlowAnalyzer;

        public InnerControlFlowAnalyzer(ControlFlowAnalyzer controlFlowAnalyzer) {
            super(new SourceInterpreter());
            this.controlFlowAnalyzer = controlFlowAnalyzer;
        }

        @Override
        protected void newControlFlowEdge(int insnIndex, int successorIndex) {
            controlFlowAnalyzer.handleControlFlowEdge(insnIndex, successorIndex);
        }

        @Override
        protected boolean newControlFlowExceptionEdge(int insnIndex, int successorIndex) {
            controlFlowAnalyzer.handleControlFlowExceptionEdge(insnIndex, successorIndex);
            return super.newControlFlowExceptionEdge(insnIndex, successorIndex);
        }

    }

}
