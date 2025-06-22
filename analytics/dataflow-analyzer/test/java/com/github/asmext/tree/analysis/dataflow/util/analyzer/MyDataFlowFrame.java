package com.github.asmext.tree.analysis.dataflow.util.analyzer;

import com.github.asmext.tree.analysis.dataflow.DataFlowFrame;
import com.github.asmext.tree.analysis.dataflow.value.DataFlowValue;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.Interpreter;

public class MyDataFlowFrame extends DataFlowFrame {
    final Runnable mergeActor;
    public MyDataFlowFrame(Frame<? extends DataFlowValue> frame, Runnable mergeActor) {
        super(frame);
        this.mergeActor = mergeActor;
    }

    public MyDataFlowFrame(int numLocals, int numStack, Runnable mergeActor) {
        super(numLocals, numStack);
        this.mergeActor = mergeActor;
    }

    @Override
    public boolean merge(Frame<? extends DataFlowValue> frame0, Interpreter<DataFlowValue> interpreter) throws AnalyzerException {
        mergeActor.run();
        return super.merge(frame0, interpreter);
    }

    @Override
    public boolean merge(Frame<? extends DataFlowValue> frame, boolean[] localsUsed) {
        mergeActor.run();
        return super.merge(frame, localsUsed);
    }
}
