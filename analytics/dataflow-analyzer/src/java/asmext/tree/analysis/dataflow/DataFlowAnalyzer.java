package asmext.tree.analysis.dataflow;

import asmext.analytics.AnalyzerPlugin;
import asmext.analytics.PlugableAnalyzer;
import asmext.analytics.PlugableAnalyzerFrame;
import asmext.tree.analysis.dataflow.interpreter.DataFlowInterpreter;
import asmext.tree.analysis.dataflow.util.ValueUtil;
import asmext.tree.analysis.dataflow.value.CommonDataFlowValue;
import asmext.tree.analysis.dataflow.value.DataFlowValue;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Function;

/**
 * An {@link Analyzer} implementation that performs data flow analysis
 * using a custom {@link DataFlowInterpreter} and specialized {@link DataFlowFrame}'s.
 * <p>
 * This analyzer builds on top of ASM's symbolic execution framework to provide
 * enhanced tracking of JVM bytecode semantics, metadata, and control flow.
 * <p>
 * Internally, it uses:
 * <ul>
 *     <li>{@link DataFlowInterpreter} — to interpret bytecode instructions and generate {@link DataFlowValue}s</li>
 *     <li>{@link DataFlowFrame} — to represent stack and local variable state during analysis</li>
 * </ul>
 *
 * <p>
 * You can subclass or invoke this analyzer to perform static analysis passes,
 * bytecode transformation validations, or build higher-level control/data flow representations.
 *
 * @author Zelaux
 * @see org.objectweb.asm.tree.analysis.Analyzer
 * @see DataFlowInterpreter
 * @see DataFlowFrame
 * @since 2025.06
 */
public class DataFlowAnalyzer extends PlugableAnalyzer<DataFlowValue> {
    public final AnalyzerPlugin<DataFlowValue> myPlugin;
    public final boolean shouldSetUsageOfValues;
    @Getter
    protected final int[] mergeFrameIndex = new int[1];
    @Getter
    protected MethodNode methodNode;

    {
        final int[] frameIndex = mergeFrameIndex;
        plugins(myPlugin = new AnalyzerPlugin<>(DataFlowValue.class) {
            @Override
            public void beforeMerge(PlugableAnalyzer<DataFlowValue> analyzer, PlugableAnalyzerFrame<? extends DataFlowValue> thisFrame, PlugableAnalyzerFrame<? extends DataFlowValue> otherFrame) {
                frameIndex[0] = thisFrame.getIndex();
            }
        });
    }

    public DataFlowAnalyzer(boolean shouldSetUsageOfValues) {
        this(new DataFlowInterpreter(), shouldSetUsageOfValues);
    }

    public DataFlowAnalyzer(DataFlowInterpreter interpreter, boolean shouldSetUsageOfValues) {
        super(interpreter);
        this.shouldSetUsageOfValues = shouldSetUsageOfValues;
        interpreter.setMergeFrameIndex(mergeFrameIndex);
    }

    private static DataFlowFrame getFrame(Frame<DataFlowValue>[] frames, int insnIndex) {
        return (DataFlowFrame) frames[insnIndex];
    }

    @Override
    public DataFlowFrame[] analyze(String owner, MethodNode node) throws AnalyzerException {
        methodNode = node;
        Frame<DataFlowValue>[] analyze = super.analyze(owner, node);
        if (shouldSetUsageOfValues) {
            return framesWithUsageValues();
        }
        return Arrays.copyOf(analyze, analyze.length, DataFlowFrame[].class);
    }

    public DataFlowFrame[] framesWithUsageValues() {
        var oldFrames = getFrames();
        var newFrames = new DataFlowFrame[oldFrames.length];
        record ValueDesc(ArrayList<Integer> frames, DataFlowValue value) {
            ValueDesc(DataFlowValue value) {
                this(new ArrayList<>(), value);
            }

            public ValueDesc addFrame(int frame) {
                if (!frames.contains(frame)) {
                    frames.add(frame);
                }
                return this;
            }

            @Override
            public boolean equals(Object obj) {
                if (!(obj instanceof ValueDesc valueDesc)) return false;
                return value == valueDesc.value;
            }
        }
        var valuesByInx = new HashMap<Integer, HashMap<ValueDesc, ValueDesc>>();
        for (int i = 0; i < oldFrames.length; i++) {
            DataFlowFrame oldFrame = (DataFlowFrame) oldFrames[i];
            if (oldFrame != null) {
                for (DataFlowValue value : oldFrame.variablesAndStack()) {
                    if (value != null) {
                        ValueDesc key = new ValueDesc(value);
                        valuesByInx
                                .computeIfAbsent(ValueUtil.getIndex(value), x -> new HashMap<>())
                                .computeIfAbsent(key, Function.identity()).addFrame(i);
                    }
                    if (value instanceof CommonDataFlowValue commonDataFlowValue) {
                        for (@NotNull DataFlowValue flowValue : commonDataFlowValue.previousValues) {
                            flowValue.addNext(commonDataFlowValue);
                        }
                    }
                }
            }
            newFrames[i] = oldFrame;
        }

        return newFrames;
    }


    @Override
    protected void newControlFlowEdge(int insnIndex, int successorIndex) {
        super.newControlFlowEdge(insnIndex, successorIndex);
        Frame<DataFlowValue>[] frames = getFrames();

        getFrame(frames, insnIndex).setGotoFrame(getFrame(frames, successorIndex));
    }

    @Override
    protected DataFlowFrame newFrame(int numLocals, int numStack) {
        return new DataFlowFrame(numLocals, numStack, this);
    }

    @Override
    public DataFlowAnalyzer plugins(@NonNull AnalyzerPlugin<? super DataFlowValue>... plugins) {
        super.plugins(plugins);
        return this;
    }

    @Override
    protected DataFlowFrame newFrame(Frame<? extends DataFlowValue> frame) {
        return new DataFlowFrame(frame, this);
    }

}
