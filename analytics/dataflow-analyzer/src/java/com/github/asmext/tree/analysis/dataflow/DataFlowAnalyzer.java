package com.github.asmext.tree.analysis.dataflow;

import com.github.asmext.tree.analysis.dataflow.interpreter.DataFlowInterpreter;
import com.github.asmext.tree.analysis.dataflow.value.DataFlowValue;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.Frame;
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
public class DataFlowAnalyzer extends Analyzer<DataFlowValue> {

    public DataFlowAnalyzer() {
        super(new DataFlowInterpreter());
    }


    @Override
    protected void newControlFlowEdge(int insnIndex, int successorIndex) {
        super.newControlFlowEdge(insnIndex, successorIndex);
    }

    @Override
    protected Frame<DataFlowValue> newFrame(int numLocals, int numStack) {
        return new DataFlowFrame(numLocals, numStack);
    }

    @Override
    protected Frame<DataFlowValue> newFrame(Frame<? extends DataFlowValue> frame) {
        return new DataFlowFrame(frame);
    }

}
