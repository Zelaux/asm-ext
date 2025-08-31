package asmext.tree.analysis.dataflow.interpreter.handlers;

import asmext.tree.analysis.dataflow.DataFlowFrame;
import asmext.tree.analysis.dataflow.DupType;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.Interpreter;
import org.objectweb.asm.tree.analysis.Value;
/**
 * Provides custom handling for JVM duplication instructions ({@code DUP}, {@code DUP_X1}, {@code DUP2}, etc.)
 * during bytecode analysis or interpretation.
 *
 * <p>The {@link #dupOperation(AbstractInsnNode, DupType, Value, DupType.ObjectKind, int)} method is typically
 * invoked from {@link DataFlowFrame#execute(AbstractInsnNode, Interpreter)} during symbolic execution or data flow analysis.
 * This happens only if the current {@link Interpreter} implementation supports this interface.</p>
 *
 * @see DupType
 * @see DupType.DupCallback
 * @see DupType#processDuplication(AbstractInsnNode, Frame, DupType.DupCallback, int, int)
 * @see DataFlowFrame#execute(AbstractInsnNode, Interpreter)
 */
public interface DupOpcodeHandler<T extends Value> {
    /**
     * Processes a duplication operation on the given value according to the specified duplication type(see {@link DupType}).
     *
     * <p>This method is typically invoked from {@link DataFlowFrame#execute(AbstractInsnNode, Interpreter)} while analyzing duplication instructions {@code DUP}, {@code DUP_X1}, {@code DUP2}, etc.</p>
     *
     * @param insn the bytecode instruction node representing the duplication operation
     * @param dupType the type of duplication instruction being processed, see {@link DupType}
     * @param value the value on the operand stack to be duplicated
     * @param kind the role of the value in the duplication pattern, see {@link DupType.ObjectKind}
     * @param index the index within the duplication sequence, useful for multi-value duplications
     *
     * @return the resulting duplicated value after applying the duplication logic
     *
     * @throws AnalyzerException if the duplication operation cannot be performed due to invalid state or type
     */
    T dupOperation(AbstractInsnNode insn, DupType dupType, T value, DupType.ObjectKind kind, int index) throws AnalyzerException;
}
