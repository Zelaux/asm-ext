package asmext.tree.analysis.dataflow.interpreter.handlers;

import asmext.tree.analysis.dataflow.DataFlowFrame;
import asmext.tree.analysis.dataflow.meta.SwapMeta;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Interpreter;
import org.objectweb.asm.tree.analysis.Value;

/**
 * Interface for handling the {@code SWAP} instruction during bytecode analysis.
 * <p>
 * Allows custom logic to be applied when the top two values on the operand stack are swapped.
 * Called from {@link DataFlowFrame#execute(AbstractInsnNode, Interpreter)} if an {@link Interpreter} implements this interface.
 *
 * @param <T> a subtype of {@link Value} used during symbolic execution
 * @see DataFlowFrame#execute(AbstractInsnNode, Interpreter)
 * @see #handleSwapOpcode(AbstractInsnNode, Value, Value, SwapResult)
 */
public interface SwapOpcodeHandler<T extends Value> {
    /**
     * Called to compute the two values resulting from a {@code SWAP} bytecode instruction.
     * <p>
     * Given two values on the operand stack — {@code bottomValue} and {@code topValue} — this method
     * returns their swapped representations via the provided {@link SwapResult} container.
     *
     * <pre>
     * Stack before: ..., bottomValue, topValue
     * Stack after : ...,     top    ,  bottom
     * </pre>
     *
     * <p>
     *
     * @param swapInsn the {@code SWAP} instruction being processed
     * @param bottomValue the second-from-top value on the stack before the swap
     * @param topValue the top value on the stack before the swap
     * @param result reusable {@link SwapResult} container to populate with swapped values
     * @return the same {@code result} instance, populated with {@code bottom} and {@code top} in their new order
     * @throws AnalyzerException if the operation cannot be completed
     *
     * @see SwapMeta
     * @see SwapResult
     * @see DataFlowFrame#execute(AbstractInsnNode, Interpreter)
     * @see SwapOpcodeHandler#handleSwapOpcode(AbstractInsnNode, Value, Value, SwapResult)
     */
    SwapResult<T> handleSwapOpcode(AbstractInsnNode swapInsn, T bottomValue, T topValue,@NotNull SwapResult<T> result) throws AnalyzerException;
    /**
     * Container for the result of a {@link SwapOpcodeHandler#handleSwapOpcode(AbstractInsnNode, Value, Value, SwapResult)}
     *
     * @param <T> the type of stack value
     *
     * @see SwapOpcodeHandler#handleSwapOpcode(AbstractInsnNode, Value, Value, SwapResult)
     */
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    final class SwapResult<T extends Value> {
        public T bottom;
        public T top;

        public SwapResult<T> reset() {
            top = bottom = null;
            return this;
        }

        public SwapResult<T> set(T bottom, T top) {
            this.top = top;
            this.bottom = bottom;
            return this;
        }


    }
}
