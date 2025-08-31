package asmext.tree.analysis.dataflow.interpreter.handlers;

import asmext.tree.analysis.dataflow.DataFlowFrame;
import asmext.tree.analysis.dataflow.value.DataFlowValue;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.analysis.Frame;

public interface CustomOpcodeHandler {
    void beforeOperation(DataFlowFrame frame,AbstractInsnNode insn) ;
    void afterOperation(DataFlowFrame frame,AbstractInsnNode insn) ;

    void beforeMerge(DataFlowFrame dataFlowFrame, Frame<? extends DataFlowValue> frame, int index);
    void afterMerge(DataFlowFrame dataFlowFrame, Frame<? extends DataFlowValue> frame, int index,boolean changed);
}
