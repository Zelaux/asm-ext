package com.github.asmext.tree.analysis.dataflow;

import com.github.asmext.tree.analysis.dataflow.interpreter.DataFlowInterpreter;
import com.github.asmext.tree.analysis.dataflow.value.DataFlowValue;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.Frame;

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
