package asmext.tree.analysis.dataflow.util.analyzer;

import asmext.tree.analysis.dataflow.DataFlowAnalyzer;
import asmext.tree.analysis.dataflow.DataFlowFrame;
import asmext.tree.analysis.dataflow.value.DataFlowValue;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.Interpreter;

public class MyDataFlowFrame extends DataFlowFrame {
    final Runnable mergeActor;
    public MyDataFlowFrame(Frame<? extends DataFlowValue> frame, Runnable mergeActor, DataFlowAnalyzer analyzer) {
        super(frame,analyzer);
        this.mergeActor = mergeActor;
    }

    public MyDataFlowFrame(int numLocals, int numStack, Runnable mergeActor, DataFlowAnalyzer analyzer) {
        super(numLocals, numStack, analyzer);
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
