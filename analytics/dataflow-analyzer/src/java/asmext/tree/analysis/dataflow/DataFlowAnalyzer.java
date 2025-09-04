package asmext.tree.analysis.dataflow;

import asmext.analytics.AnalyzerPlugin;
import asmext.analytics.PlugableAnalyzer;
import asmext.tree.analysis.dataflow.interpreter.DataFlowInterpreter;
import asmext.tree.analysis.dataflow.value.DataFlowValue;
import lombok.NonNull;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;

import java.util.Arrays;

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
 * @since 2025.06
 *
 * @see org.objectweb.asm.tree.analysis.Analyzer
 * @see DataFlowInterpreter
 * @see DataFlowFrame
 */
public class DataFlowAnalyzer extends PlugableAnalyzer<DataFlowValue> {

    public DataFlowAnalyzer() {
        this(new DataFlowInterpreter());
    }

    public DataFlowAnalyzer(DataFlowInterpreter interpreter) {
        super(interpreter);
    }

    @Override
    public DataFlowFrame[] analyze(String owner, MethodNode node) throws AnalyzerException {
        return Arrays.copyOf(super.analyze(owner, node), super.analyze(owner, node).length, DataFlowFrame[].class);
    }

    @Override
    protected void newControlFlowEdge(int insnIndex, int successorIndex) {
        super.newControlFlowEdge(insnIndex, successorIndex);
        Frame<DataFlowValue>[] frames = getFrames();

        getFrame(frames, insnIndex).setGotoFrame(getFrame(frames,successorIndex));
    }

    private static DataFlowFrame getFrame(Frame<DataFlowValue>[] frames, int insnIndex) {
        return (DataFlowFrame) frames[insnIndex];
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
        return new DataFlowFrame(frame,this);
    }

}
