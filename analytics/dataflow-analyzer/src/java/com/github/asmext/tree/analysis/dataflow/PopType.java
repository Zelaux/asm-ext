package com.github.asmext.tree.analysis.dataflow;

import com.github.asmext.tree.analysis.dataflow.interpreter.handlers.PopOpcodeHandler;
import lombok.AllArgsConstructor;
import org.intellij.lang.annotations.MagicConstant;
import org.objectweb.asm.Opcodes;

/**
 * Represents the type of pop operation used by {@code POP} and {@code POP2} JVM instructions.
 *
 * <p>These instructions behave differently based on the *category* of the values on the operand stack:
 * category 1 (occupying one stack slot) or category 2 (occupying two slots).
 *
 * <p>The table below is taken from the Java Virtual Machine Specification (§2.11.1)
 * and describes how actual Java types are treated computationally on the operand stack:
 *
 * <pre>
 * | Actual type     | Computational type | Category |
 * |-----------------|--------------------|----------|
 * | boolean         | int                |    1     |
 * | byte            | int                |    1     |
 * | char            | int                |    1     |
 * | short           | int                |    1     |
 * | int             | int                |    1     |
 * | float           | float              |    1     |
 * | reference       | reference          |    1     |
 * | returnAddress   | returnAddress      |    1     |
 * | <b>long</b>            | <b>long</b>               |    <b>2</b>     |
 * | <b>double</b>          | <b>double</b>             |    <b>2</b>     |
 * </pre>
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-2.html#jvms-2.11.1-320">
 * Java Virtual Machine Specification §2.11.1</a>
 * @see PopOpcodeHandler
 * @see DataFlowFrame#execute(org.objectweb.asm.tree.AbstractInsnNode, org.objectweb.asm.tree.analysis.Interpreter)
 */
@AllArgsConstructor
public enum PopType {
    /**
     * Represents the {@code POP} instruction, which removes a single value of category 1
     * (e.g., {@code int}, {@code float}, {@code reference}).
     *
     * @see <a href="https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-2.html#jvms-2.11.1">JVM Spec §2.11.1</a>
     * @see <a href="https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-6.html#jvms-6.5.pop">JVM Spec §6.5 POP</a>
     */
    POP_1(Opcodes.POP, 1, 1),
    /**
     * Represents the {@code POP2} instruction when it removes a single value of category 2
     * (e.g., {@code long} or {@code double}).
     *
     * @see <a href="https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-2.html#jvms-2.11.1">JVM Spec §2.11.1</a>
     * @see <a href="https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-6.html#jvms-6.5.pop2">JVM Spec §6.5 POP2</a>
     */
    POP_2(Opcodes.POP2, 1, 2),
    /**
     * Represents the {@code POP2} instruction when it removes two values of category 1.
     * Equivalent to performing two consecutive {@code POP} operations.
     *
     * @see <a href="https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-2.11.1">JVM Spec §2.11.1</a>
     * @see <a href="https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-6.html#jvms-6.5.pop2">JVM Spec §6.5 POP2</a>
     */
    POP_11(Opcodes.POP2, 2, 1),
    ;
    @MagicConstant(intValues = {Opcodes.POP,Opcodes.POP2})
    public final int opcode;
    /** Number of values popped from the operand stack. */
    public final int amount;
    /** Size (in stack slots) of each individual popped value. */
    public final int sizeEach;
}
