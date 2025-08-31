package asmext.tree.analysis.dataflow.interpreter;

import asmext.tree.analysis.dataflow.DataFlowFrame;
import org.objectweb.asm.tree.AbstractInsnNode;

public interface OpcodeHandler {

    default void beforeOperation(
            DataFlowInterpreter interpreter,
            DataFlowFrame frame,
            AbstractInsnNode insn) {}

    default void afterOperation(
            DataFlowInterpreter interpreter,
            DataFlowFrame frame,
            AbstractInsnNode insn) {}

    default void beforeMerge(DataFlowInterpreter interpreter, DataFlowFrame sourceFrame, DataFlowFrame frame, int index) {}

    default void afterMerge(DataFlowInterpreter interpreter, DataFlowFrame sourceFrame, DataFlowFrame frame, int index, boolean changed){}
}
