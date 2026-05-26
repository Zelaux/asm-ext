package asmext.analytics;

import asmext.analytics.controlflow.ControlFlowBlock;
import asmext.analytics.controlflow.ControlFlowNode;
import asmext.analytics.controlflow.ControlFlowPlugin;
import asmext.analytics.controlflow.InsnIdxSet;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.SourceValue;
import org.objectweb.asm.tree.analysis.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class BooleanComparisonSearchPlugin<V extends Value> extends AnalyzerPlugin<V> {
    public static final InsnList MOCK_LIST = new InsnList();
    public static final AbstractInsnNode[] TMP_ARRAY = new AbstractInsnNode[2];
    public final ControlFlowPlugin controlFlowPlugin;
    public final ValueToMergedNodes<V> mergedNodes;
//    public AbstractInsnNode[] instructions;
    boolean[] shouldReview;
    int totalShouldReview = 0;
    private ArrayList<BooleanComparison> comparison = new ArrayList<>();

    public BooleanComparisonSearchPlugin(ControlFlowPlugin controlFlowPlugin, ValueToMergedNodes<V> mergedNodes, Class<V> type) {
        super(type);
        this.controlFlowPlugin = controlFlowPlugin;
        this.mergedNodes = mergedNodes;

    }

    @Override
    public void initialFrame(PlugableAnalyzer<V> analyzer, PlugableAnalyzerFrame<V> frame) {
        setInstructions(analyzer.currentInstructions);
        super.initialFrame(analyzer, frame);
    }

    private static boolean checkIsNumber(AbstractInsnNode insnNode, int[] valueCounter, int index) {
        if (!(insnNode instanceof InsnNode value)) return false;

        int i = switch (value.getOpcode()) {
            case Opcodes.ICONST_0 -> 0;
            case Opcodes.ICONST_1 -> 1;
            default -> -1;
        };
        if (i == -1) return false;
        if (valueCounter[i] >= 0) return false;
        valueCounter[i] = index;
        return true;
    }

    private static int idx(AbstractInsnNode insnNode) {
        return MOCK_LIST.indexOf(insnNode);
    }

    private static boolean isIfOpcode(int opcode) {
        return (Opcodes.IFEQ <= opcode && opcode <= Opcodes.IF_ACMPNE) || opcode == Opcodes.IFNULL || opcode == Opcodes.IFNONNULL;
    }

    public BooleanComparisonSearchPlugin<SourceValue> sourceValue(ControlFlowPlugin controlFlowPlugin) {
        return new BooleanComparisonSearchPlugin<>(controlFlowPlugin, ValueToMergedNodes.sourceValueNodes, SourceValue.class);
    }

    private int[] isBooleanValue(V topValue) {
        var mergedNodes = this.mergedNodes;
        if (mergedNodes.amoutOfMerged(topValue) != 2) return null;
        mergedNodes.copyMergedInsnToArray(topValue, TMP_ARRAY);
        int[] hasNumber = {-1, -1};
        for (int i = 0; i < TMP_ARRAY.length; i++) {
            if (!checkIsNumber(TMP_ARRAY[i], hasNumber, i)) return null;
        }
        return hasNumber;
    }

    public BooleanComparisonSearchPlugin setInstructions(AbstractInsnNode... instructions) {
        this.mergedNodes.setInstructions(instructions);
        comparison.clear();
        return this;
    }

    @Override
    public void afterMerge(PlugableAnalyzer<V> analyzer, PlugableAnalyzerFrame<? extends V> thisFrame, PlugableAnalyzerFrame<? extends V> otherFrame, boolean changed) {
        V topValue = thisFrame.valueOnTop();
        if (isBooleanValue(topValue) == null) return;
        if (shouldReview[thisFrame.getIndex()]) return;
        shouldReview[thisFrame.getIndex()] = true;
        totalShouldReview++;
//        System.out.println(hasNumber.length);
    }

    @Override
    public void beforeAnalyze(PlugableAnalyzer<V> analyzer, String owner, MethodNode method) {
        shouldReview = new boolean[method.instructions.size()];
        setInstructions(method.instructions.toArray());
        totalShouldReview = -1;
        super.beforeAnalyze(analyzer, owner, method);
    }

    @Override
    public void afterAnalyze(PlugableAnalyzer<V> analyzer, String owner, MethodNode method, Frame<V>[] analyzed) {
        if (totalShouldReview == 0) return;
        var blocks = controlFlowPlugin.collectGroups();
        //noinspection unchecked
        var idxToBlock = new ControlFlowBlock[shouldReview.length];
        for (ControlFlowBlock block : blocks) {
            int endIdx = block.last.myIndex;
            for (int i = block.first.myIndex; i <= endIdx; i++) {
                idxToBlock[i] = block;
            }
        }
        final int[] tmpArray = new int[2];
        final AbstractInsnNode[] tmpNodesArr = TMP_ARRAY;
        HashMap<Set<AbstractInsnNode>, BooleanComparison> nodesToComparison = new HashMap<>();
        for (int i = 0; i < shouldReview.length && totalShouldReview > 0; i++) {
            if (!shouldReview[i]) continue;
            V topValue = analyzer.castFrame(analyzed[i]).valueOnTop();
            totalShouldReview--;
            var boolToIdx = isBooleanValue(topValue);
            if (boolToIdx == null) continue;

            mergedNodes.copyMergedInsnToArray(topValue,tmpNodesArr);
            ControlFlowBlock afterIfBlock = idxToBlock[i];
            InsnIdxSet previous = afterIfBlock.previous;
            if (previous.size() != 2) continue;
            int[] branches = previous.toArray(tmpArray);
            ControlFlowBlock falseBlock = idxToBlock[branches[boolToIdx[0]]];
            if (idxToBlock[idx(tmpNodesArr[boolToIdx[0]])] != falseBlock) continue;
            ControlFlowBlock trueBlock = idxToBlock[branches[boolToIdx[1]]];
            if (idxToBlock[idx(tmpNodesArr[boolToIdx[1]])] != trueBlock) continue;

            if (falseBlock.previous.size() != 1) continue;
            if (trueBlock.previous.size() != 1) continue;
            int ifStmtIdx = trueBlock.previous.first();
            if (ifStmtIdx != falseBlock.previous.first()) continue;

            ControlFlowNode ifNode = this.controlFlowPlugin.nodes()[ifStmtIdx];
            if (!(ifNode.node instanceof JumpInsnNode jumpInsnNode)) continue;
            if (!isIfOpcode(jumpInsnNode.getOpcode())) continue;

            this.comparison.add(new BooleanComparison(
                    ifStmtIdx,
                    trueBlock.startIdx(), trueBlock.endIdx(), idx(tmpNodesArr[boolToIdx[1]]),
                    falseBlock.startIdx(), falseBlock.endIdx(), idx(tmpNodesArr[boolToIdx[0]]),
                    i
            ));
//            for (AbstractInsnNode insn : topValue.insns) {
//                int index = MOCK_LIST.indexOf(insn);
//
//            }
        }
    }

    private void addIndexToComparasion(BooleanComparison booleanComparison, int i) {
        throw null;
    }

    public ArrayList<BooleanComparison> allComparison() {
        return this.comparison;
    }
}
