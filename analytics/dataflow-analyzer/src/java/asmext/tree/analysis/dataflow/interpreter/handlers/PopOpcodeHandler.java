package asmext.tree.analysis.dataflow.interpreter.handlers;

import asmext.tree.analysis.dataflow.DataFlowFrame;
import asmext.tree.analysis.dataflow.PopType;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.analysis.Interpreter;
import org.objectweb.asm.tree.analysis.Value;
/**
 * Interface for handling metadata or tracking when values are popped from the stack
 * by specific pop-related bytecode instructions ({@code POP}, {@code POP2}).
 *
 * <p>
 * This interface allows custom logic to be invoked whenever values are removed from
 * the operand stack during interpretation. Typical usage includes associating
 * additional metadata (e.g., for debugging, analysis, or transformation) with
 * values that are explicitly popped by instructions.
 *
 * <p>
 * Used by {@link DataFlowFrame} during the execution of {@code POP} and {@code POP2}.
 *
 * @see DataFlowFrame#execute(AbstractInsnNode, Interpreter)
 * @see PopType
 */
public interface PopOpcodeHandler<T extends Value> {
    /**
     * Called by {@link DataFlowFrame} when a value is explicitly removed (popped) from the operand stack
     * by a {@code POP} or {@code POP2} instruction during bytecode interpretation.
     *
     *
     * @param insn the {@link AbstractInsnNode} representing the POP or POP2 instruction
     * @param pop the value being removed from the operand stack
     * @param popType the type of pop operation
     * @param i the index of the value relative to the instruction's effect (0 or 1)
     *
     * @see PopType
     * @see DataFlowFrame#execute(AbstractInsnNode, Interpreter)
     */
    void popped(AbstractInsnNode insn, T pop, PopType popType, int i);
}
