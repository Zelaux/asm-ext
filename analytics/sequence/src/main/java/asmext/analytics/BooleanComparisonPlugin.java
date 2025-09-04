package asmext.analytics;

import asmext.analytics.controlflow.*;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.SourceValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class BooleanComparisonPlugin extends AnalyzerPlugin<SourceValue> {
    public static final InsnList MOCK_LIST = new InsnList();
    public final ControlFlowPlugin controlFlowPlugin;
    public AbstractInsnNode[] instructions;
    boolean[] shouldReview;
    int totalShouldReview = 0;
    private ArrayList<BooleanComparison> comparison = new ArrayList<>();

    public BooleanComparisonPlugin(ControlFlowPlugin controlFlowPlugin) {
        super(SourceValue.class);
        this.controlFlowPlugin = controlFlowPlugin;
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

    private static int[] isBooleanValue(SourceValue topValue) {
        if (topValue.insns.size() != 2) return null;
        var iterator = topValue.insns.iterator();
        int[] hasNumber = {-1, -1};

        if (!checkIsNumber(iterator.next(), hasNumber, 0)) return null;
        if (!checkIsNumber(iterator.next(), hasNumber, 1)) return null;
        return hasNumber;
    }

    private static int idx(AbstractInsnNode insnNode) {
        return MOCK_LIST.indexOf(insnNode);
    }

    public BooleanComparisonPlugin setInstructions(AbstractInsnNode... instructions) {
        this.instructions = instructions;
        comparison.clear();
        return this;
    }

    @Override
    public void afterMerge(PlugableAnalyzer<SourceValue> analyzer, PlugableAnalyzerFrame<? extends SourceValue> thisFrame, PlugableAnalyzerFrame<? extends SourceValue> otherFrame, boolean changed) {
        SourceValue topValue = thisFrame.valueOnTop();
        if (isBooleanValue(topValue) == null) return;
        if (shouldReview[thisFrame.index]) return;
        shouldReview[thisFrame.index] = true;
        totalShouldReview++;
//        System.out.println(hasNumber.length);
    }

    @Override
    public void beforeAnalyze(PlugableAnalyzer<SourceValue> analyzer, String owner, MethodNode method) {
        shouldReview = new boolean[method.instructions.size()];
        totalShouldReview = -1;
        super.beforeAnalyze(analyzer, owner, method);
    }

    @Override
    public void afterAnalyze(PlugableAnalyzer<SourceValue> analyzer, String owner, MethodNode method, Frame<SourceValue>[] analyzed) {
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
        final AbstractInsnNode[] tmpNodesArr = new AbstractInsnNode[2];
        HashMap<Set<AbstractInsnNode>, BooleanComparison> nodesToComparison = new HashMap<>();
        for (int i = 0; i < shouldReview.length && totalShouldReview > 0; i++) {
            if (!shouldReview[i]) continue;
            SourceValue topValue = analyzer.castFrame(analyzed[i]).valueOnTop();
            totalShouldReview--;
            var boolToIdx = isBooleanValue(topValue);
            if (boolToIdx == null) continue;

            topValue.insns.toArray(tmpNodesArr);
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
                    trueBlock.startIdx(),trueBlock.endIdx(),idx(tmpNodesArr[boolToIdx[1]]),
                    falseBlock.startIdx(),falseBlock.endIdx(),idx(tmpNodesArr[boolToIdx[0]]),
                    i
            ));
//            for (AbstractInsnNode insn : topValue.insns) {
//                int index = MOCK_LIST.indexOf(insn);
//
//            }
        }
    }

    private static boolean isIfOpcode(int opcode) {
        return (Opcodes.IFEQ <= opcode && opcode <= Opcodes.IF_ACMPNE) || opcode == Opcodes.IFNULL || opcode == Opcodes.IFNONNULL;
    }

    private void addIndexToComparasion(BooleanComparison booleanComparison, int i) {
        throw null;
    }

    public ArrayList<BooleanComparison> allComparison() {
        return this.comparison;
    }
}
