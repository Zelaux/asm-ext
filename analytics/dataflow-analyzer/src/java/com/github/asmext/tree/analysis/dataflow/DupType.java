package com.github.asmext.tree.analysis.dataflow;

import com.github.asmext.tree.analysis.dataflow.interpreter.handlers.DupOpcodeHandler;
import lombok.RequiredArgsConstructor;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.Value;

import static com.github.asmext.tree.analysis.dataflow.DataFlowFrame.illegalUse;
/**
 * Enum representing JVM bytecode duplication instructions such as {@code DUP}, {@code DUP2}, and their variants.
 *
 * <p>Stack notation:
 * <ul>
 *   <li>{@code o1, o2} — original values at the top of the stack (o1 is topmost)</li>
 *   <li>{@code m1, m2} — middle values over which duplicates are inserted</li>
 *   <li>{@code d1, d2} — duplicates of {@code o1, o2}</li>
 * </ul>
 *
 * @see #processDuplication(AbstractInsnNode, Frame, DupCallback, int, int)
 * @see DupOpcodeHandler
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-6.html#jvms-6.5.dup">JVM Spec §6.5 DUP</a>
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-6.html#jvms-6.5.dup_x1">JVM Spec §6.5 DUP_X1</a>
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-6.html#jvms-6.5.dup_x2">JVM Spec §6.5 DUP_X2</a>
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-6.html#jvms-6.5.dup2">JVM Spec §6.5 DUP2</a>
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-6.html#jvms-6.5.dup2_x1">JVM Spec §6.5 DUP2_X1</a>
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-6.html#jvms-6.5.dup2_x2">JVM Spec §6.5 DUP2_X2</a>
 * */
@RequiredArgsConstructor
public enum DupType {

    /** [..., o1] → [..., o1, d1] */
    DUP(Opcodes.DUP, 1, 0),

    /** [..., m1, o1] → [..., d1, m1, o1] */
    DUP_X1(Opcodes.DUP_X1, 1, 1),

    /** [..., m2, m1, o1] → [..., d1, m2, m1, o1] */
    DUP_X2(Opcodes.DUP_X2, 1, 2),

    /** [..., o2, o1] → [..., o2, o1, d2, d1] */
    DUP2(Opcodes.DUP2, 2, 0),

    /** [..., m1, o2, o1] → [..., d2, d1, m1, o2, o1] */
    DUP2_X1(Opcodes.DUP2_X1, 2, 1),

    /** [..., m2, m1, o2, o1] → [..., d2, d1, m2, m1, o2, o1] */
    DUP2_X2(Opcodes.DUP2_X2, 2, 2);
    public static final DupType[] VALUES = values();
    public final int opcode;
    @MagicConstant(intValues = {1, 2})
    public final int size;
    @MagicConstant(intValues = {0, 1, 2})
    public final int shift;

    @Nullable
    public static DupType fromOpcode(int opcode) {
        if(opcode < Opcodes.DUP || opcode > Opcodes.DUP2_X2) return null;
        return VALUES[opcode - Opcodes.DUP];
    }

    /**
     * Performs a generic duplication transformation, modeling the effect of a {@code DUP}-style bytecode instruction.
     * <p>
     * Pops the necessary values from the frame, verifies type sizes (single or double-word), and invokes
     * the given {@link DupCallback} with values in the order they would be re-pushed to the stack.
     *
     * @param insn                the instruction node representing the duplication opcode
     * @param frame               the analysis frame representing the current stack
     * @param callback            a callback invoked with values in push order
     * @param sizeOfDuplication   number of values to duplicate (1 or 2)
     * @param shiftOfDuplication  how deep to insert the duplicate (0–2)
     * @throws AnalyzerException if stack state or types are invalid
     */
    public static <T extends Value> void processDuplication(AbstractInsnNode insn, Frame<T> frame, DupCallback<T> callback, int sizeOfDuplication, int shiftOfDuplication) throws AnalyzerException {
        T read1 = frame.pop();
        T read2 = null;
        boolean oneRead;
        {
            if(sizeOfDuplication == 1 && read1.getSize() == 2) throw illegalUse(sizeMismatch(1, 2), insn);
            //noinspection MagicConstant
            if(read1.getSize() == sizeOfDuplication) {
                oneRead = true;
            } else {
                oneRead = false;
                read2 = frame.pop();
                if(read2.getSize() == 2) throw illegalUse(sizeMismatch(1, 2), insn);
            }
        }
        switch(shiftOfDuplication) {
            case 0 -> {
                pushValue(callback, oneRead, read1, read2, 0, null, null);
            }
            case 1 -> {
                T shiftValue = frame.pop();
                if(shiftValue.getSize() == 2) throw illegalUse(sizeMismatch(1, 2), insn);
                pushValue(callback, oneRead, read1, read2, 1, shiftValue, null);
            }
            case 2 -> {
                T shift1 = frame.pop();
                if(shift1.getSize() == 2) {
                    pushValue(callback, oneRead, read1, read2, 1, shift1, null);
                } else {
                    T shift2 = frame.pop();
                    if(shift2.getSize() == 2) throw illegalUse(sizeMismatch(1, 2), insn);
                    pushValue(callback, oneRead, read1, read2, 2, shift1, shift2);
                }
            }
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static String sizeMismatch(int expected, int actual) {
        return "Expected stack top " + expected + "size but found " + actual + "size";
    }
    /**
     * Pushes values to the callback in the correct order according to the duplication semantics.
     * <p>
     * This includes original values, any middle/shifted values, and finally the duplicated copies.
     */
    private static <T extends Value> void pushValue(DupCallback<T> callback, boolean oneRead, T read1, T read2, @MagicConstant(intValues = {0, 1, 2}) int shift, T shift1, T shift2) throws AnalyzerException {
        if(oneRead) {
            callback.push(read1, ObjectKind.Original, 0);

            pushShift(callback, shift, shift1, shift2);

            callback.push(read1, ObjectKind.Duplicate, 0);
        } else {
            callback.push(read2, ObjectKind.Original, 1);
            callback.push(read1, ObjectKind.Original, 0);

            pushShift(callback, shift, shift1, shift2);

            callback.push(read2, ObjectKind.Duplicate, 1);
            callback.push(read1, ObjectKind.Duplicate, 0);
        }
    }

    private static <T extends Value> void pushShift(DupCallback<T> callback, @MagicConstant(intValues = {0, 1, 2}) int shift, T shift1, T shift2) throws AnalyzerException {
        switch(shift) {
            case 0 -> {}
            case 1 -> callback.push(shift1, ObjectKind.MiddlePart, 0);
            case 2 -> {
                callback.push(shift2, ObjectKind.MiddlePart, 1);
                callback.push(shift1, ObjectKind.MiddlePart, 0);
            }
        }
    }
    /**
     * Describes the role of a value in a {@link DupType} duplication pattern.
     */
    public enum ObjectKind {
        /**Duplicated copy inserted into the stack*/
        Duplicate,

        /** A value over which the duplicates are inserted (shift/middle part) */
        MiddlePart,
        /**Original value before duplication*/
        Original;

        public boolean shiftedPart() {
            return this == MiddlePart;
        }

        public boolean nonMiddle() {
            return this != MiddlePart;
        }
    }

    public interface DupCallback<T> {
        /**
         * Invoked once for each value involved in the duplication pattern, in the exact order
         * values would be pushed to the stack after the duplication instruction.
         *
         * @param value the value being pushed
         * @param kind  the role of the value (original, duplicate, or middle part)
         * @param index the logical index within its kind:<br>
         *              {@code 0} for {@code o1}/{@code d1}/{@code m1},<br>
         *              {@code 1} for {@code o2}/{@code d2}/{@code m2}
         *
         * @throws AnalyzerException if the push operation cannot be completed
         */
        void push(T value, ObjectKind kind,@MagicConstant(intValues = {0,1}) int index) throws AnalyzerException;
    }
}
