package com.github.asmext.tree.analysis.dataflow;

import lombok.RequiredArgsConstructor;
import org.intellij.lang.annotations.MagicConstant;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.Value;

import static com.github.asmext.tree.analysis.dataflow.DataFlowFrame.illegalUse;
/**
 * Enum representing JVM bytecode operations for duplicating values on the operand stack.<br>
 * <br>
 * Stack notation:<br>
 * - o1, o2: Original values (top of stack is o1)<br>
 * - m1, m2: Middle values (deeper in stack)<br>
 * - d1, d2: Duplicated copies of original values<br>
 * <br>
 * All operations preserve the original values and insert duplicates at specific positions.
 */
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
    private Object[] tmpArray;

    public static DupType fromOpcode(int opcode) {
        if(opcode < Opcodes.DUP || opcode > Opcodes.DUP2_X2) return null;
        return VALUES[opcode - Opcodes.DUP];
    }


    public static <T extends Value> void processDuplication(AbstractInsnNode insn, Frame<T> frame, DubCallback<T> callback, int sizeOfDuplication, int shiftOfDuplication) throws AnalyzerException {
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

    private static <T extends Value> void pushValue(DubCallback<T> callback, boolean oneRead, T read1, T read2, @MagicConstant(intValues = {0, 1, 2}) int shift, T shift1, T shift2) throws AnalyzerException {
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

    private static <T extends Value> void pushShift(DubCallback<T> callback, @MagicConstant(intValues = {0, 1, 2}) int shift, T shift1, T shift2) throws AnalyzerException {
        switch(shift) {
            case 0 -> {}
            case 1 -> callback.push(shift1, ObjectKind.MiddlePart, 0);
            case 2 -> {
                callback.push(shift2, ObjectKind.MiddlePart, 1);
                callback.push(shift1, ObjectKind.MiddlePart, 0);
            }
        }
    }

    public enum ObjectKind {
        Duplicate,
        MiddlePart,
        Original;

        public boolean shiftedPart() {
            return this == MiddlePart;
        }

        public boolean nonMiddle() {
            return this != MiddlePart;
        }
    }

    public interface DubCallback<T> {
        void push(T value, ObjectKind kind, int index) throws AnalyzerException;
    }
}
