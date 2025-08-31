package asmext.tree.analysis.dataflow;

import asmext.tree.analysis.dataflow.interpreter.DataFlowInterpreter;
import asmext.tree.analysis.dataflow.interpreter.handlers.CustomOpcodeHandler;
import asmext.tree.analysis.dataflow.interpreter.handlers.DupOpcodeHandler;
import asmext.tree.analysis.dataflow.interpreter.handlers.PopOpcodeHandler;
import asmext.tree.analysis.dataflow.interpreter.handlers.SwapOpcodeHandler;
import asmext.tree.analysis.dataflow.value.DataFlowValue;
import lombok.Getter;
import lombok.Lombok;
import lombok.SneakyThrows;
import org.intellij.lang.annotations.MagicConstant;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.Interpreter;
import org.objectweb.asm.tree.analysis.Value;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * A specialized {@link Frame} used in JVM bytecode data flow analysis.
 * <p>
 * {@code DataFlowFrame} extends ASM's {@link Frame} to add support for:
 * <ul>
 *     <li>Tracking instruction index and source indices for better diagnostics and debugging</li>
 *     <li>Handling of advanced stack operations like {@code POP}, {@code DUP}, {@code SWAP} with custom metadata</li>
 *     <li>Capturing recently popped values via {@link #justPopped} to assist post-pop analysis</li>
 *     <li>Interacting with {@link PopOpcodeHandler}, {@link DupOpcodeHandler}, and {@link SwapOpcodeHandler}
 *     for custom interpreter behavior</li>
 * </ul>
 *
 * <p>
 *
 * @author Zelaux
 * @see org.objectweb.asm.tree.analysis.Frame
 * @see DataFlowAnalyzer
 * @see DataFlowInterpreter
 * @see PopOpcodeHandler
 * @see DupOpcodeHandler
 * @see SwapOpcodeHandler
 * @see DupType
 * @see PopType
 * @since 2025.06
 */
public class DataFlowFrame extends Frame<DataFlowValue> implements Opcodes {
    private static final InsnList TMP_LIST = new InsnList();
    private final SwapOpcodeHandler.SwapResult<DataFlowValue> tmpSwapResult = new SwapOpcodeHandler.SwapResult<>();
    private final Value[] values;
    int index;
    int sourceIndex;
    @Getter
    DataFlowValue[] justPopped;
    @Getter
    private ArrayList<DataFlowFrame> jumpSourceFrames;
    @Getter
    private AbstractInsnNode insnNode;
    @Getter
    private DataFlowFrame gotoFrame;

    {
        try {
            Field field = Frame.class.getDeclaredField("values");
            field.setAccessible(true);
            values = (Value[]) field.get(this);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw Lombok.sneakyThrow(e);
        }
    }

    public DataFlowFrame(Frame<? extends DataFlowValue> frame) {
        super(frame);
    }

    public DataFlowFrame(int numLocals, int numStack) {
        super(numLocals, numStack);
        resetState();
    }

    public static AnalyzerException illegalUse(String extraInfo, AbstractInsnNode insn) throws AnalyzerException {
        String opcode = Printer.OPCODES[Math.max(0, insn.getOpcode())];
        throw new AnalyzerException(insn, "Illegal use of " + opcode + ": " + extraInfo);
    }

    private static AnalyzerException illegalUse(AbstractInsnNode insn) throws AnalyzerException {
        return illegalUse(insn, Printer.OPCODES[Math.max(0, insn.getOpcode())]);
    }

    private static AnalyzerException illegalUse(AbstractInsnNode insn, String pop1) throws AnalyzerException {
        throw new AnalyzerException(insn, "Illegal use of " + pop1);
    }

    public DataFlowValue valueOnTop() {
        return getStack(getStackSize() - 1);
    }

    @Override
    public Frame<DataFlowValue> init(Frame<? extends DataFlowValue> frame) {
        if (frame instanceof DataFlowFrame frame1) {
            index = frame1.index;//TODO find way to save index in label frames
            sourceIndex = frame1.sourceIndex;//TODO find way to save index in label frames
            justPopped = frame1.justPopped;
            gotoFrame = frame1.gotoFrame;
            jumpSourceFrames = frame1.jumpSourceFrames;
            insnNode = frame1.insnNode;
        } else {
            index = -1;
            resetState();

        }
        return super.init(frame);
    }

    public void setGotoFrame(DataFlowFrame frame) {
        this.gotoFrame = frame;
        frame.jumpSourceFrames.add(this);
    }

    private void resetState() {
        sourceIndex = index;
        index = -1;
        insnNode = null;
        justPopped = null;
        gotoFrame = null;
        jumpSourceFrames = new ArrayList<>();
    }

    @Override
    public boolean merge(Frame<? extends DataFlowValue> frame, Interpreter<DataFlowValue> interpreter) throws AnalyzerException {
        if (interpreter instanceof CustomOpcodeHandler handler) handler.beforeMerge(this, frame, index);
        boolean result = super.merge(frame, interpreter);
        if (interpreter instanceof CustomOpcodeHandler handler) handler.afterMerge(this, frame, index, result);
        return result;
    }

    /**
     * Executes a single instruction on this frame with optional metadata-aware processing for certain opcodes.
     * <p>
     * This method overrides ASM's default execution behavior to support extended semantics for stack operations,
     * specifically:
     * <ul>
     *     <li>{@code POP}, {@code POP2} — processed via {@link PopOpcodeHandler}</li>
     *     <li>{@code DUP}, {@code DUP_X1}, {@code DUP_X2}, {@code DUP2}, {@code DUP2_X1}, {@code DUP2_X2} —
     *         processed via {@link DupOpcodeHandler}</li>
     *     <li>{@code SWAP} — processed via {@link SwapOpcodeHandler}</li>
     * </ul>
     * If the provided {@link Interpreter} implements the corresponding handler interface, this method delegates
     * processing of the instruction to it, enabling custom tracking and transformation of {@link DataFlowValue} instances.
     * <p>
     * For all other opcodes, the base class implementation is used.
     *
     * <p><b>Note:</b> Also tracks the most recently popped values via {@link #justPopped} to support post-pop analysis.</p>
     *
     * @param insn        the instruction to execute
     * @param interpreter the interpreter handling symbolic execution
     * @throws AnalyzerException if the instruction is invalid or cannot be executed
     */
    @Override
    public void execute(AbstractInsnNode insn, Interpreter<DataFlowValue> interpreter) throws AnalyzerException {
        resetState();
        //noinspection MagicConstant
        @MagicConstant(valuesFromClass = Opcodes.class)
        int opcode = insn.getOpcode();

        index = TMP_LIST.indexOf(insn);
        this.insnNode = insn;
        if (interpreter instanceof CustomOpcodeHandler handler) handler.beforeOperation(this, insn);
        try {
            switch (opcode) {
                case POP -> {
                    DataFlowValue pop = pop();
                    if (pop.getSize() == 2) throw illegalUse(insn, "POP");
                    popped(interpreter, pop, insn, 0, PopType.POP_1);
                    justPopped = new DataFlowValue[]{pop};
                }
                case POP2 -> {
                    DataFlowValue pop = pop();
                    if (pop.getSize() == 2) {
                        popped(interpreter, pop, insn, 0, PopType.POP_2);
                        justPopped = new DataFlowValue[]{pop};
                    } else {
                        DataFlowValue pop2 = pop();
                        if (pop2.getSize() != 1) throw illegalUse(insn, "POP2");
                        popped(interpreter, pop, insn, 0, PopType.POP_11);
                        popped(interpreter, pop2, insn, 1, PopType.POP_11);
                        justPopped = new DataFlowValue[]{pop, pop2};
                    }
                }
                case DUP,
                     DUP_X1,
                     DUP_X2,
                     DUP2,
                     DUP2_X1,
                     DUP2_X2 -> processDup(opcode, insn, interpreter);
                case SWAP -> {
                    if (interpreter instanceof SwapOpcodeHandler<?>) {
                        DataFlowValue topValue = pop();
                        DataFlowValue bottomValue = pop();
                        if (bottomValue.getSize() != 1 || topValue.getSize() != 1) {
                            throw illegalUse(insn);
                        }
                        //noinspection unchecked
                        var x = (SwapOpcodeHandler<DataFlowValue>) interpreter;
                        SwapOpcodeHandler.SwapResult<DataFlowValue> result = x.handleSwapOpcode(insn, bottomValue, topValue, tmpSwapResult);
                        push(result.bottom);
                        push(result.top);
                        result.reset();
                    } else super.execute(insn, interpreter);
                }
                default -> super.execute(insn, interpreter);
            }
        } catch (Exception e) {
            System.out.println("Error In: " + Printer.OPCODES[insn.getOpcode()]);
            throw Lombok.sneakyThrow(e);
        }

        if (interpreter instanceof CustomOpcodeHandler handler) handler.afterOperation(this, insn);
    }

    @Override
    public DataFlowValue pop() {
        DataFlowValue pop = super.pop();
        values[getLocals() + getStackSize()] = null;
        return pop;
    }

    private void popped(Interpreter<DataFlowValue> interpreter, DataFlowValue pop, AbstractInsnNode insn, int i, PopType popType) {
        //noinspection rawtypes
        if (interpreter instanceof PopOpcodeHandler popOpcodeHandler) {
            //noinspection unchecked
            popOpcodeHandler.popped(insn, pop, popType, i);
        }
    }

    @SneakyThrows
    private void processDup(int opcode, AbstractInsnNode insn, Interpreter<DataFlowValue> interpreter) {

        DupType dupType = DupType.fromOpcode(opcode);
        assert dupType != null;
        DupType.DupCallback<DataFlowValue> callback;
        //noinspection rawtypes
        if (interpreter instanceof DupOpcodeHandler __advancedDupProcess) {
            //noinspection unchecked,UnnecessaryLocalVariable
            DupOpcodeHandler<DataFlowValue> advancedDupProcess = __advancedDupProcess;
            callback = (value, kind, index) ->
                    DataFlowFrame.this.push(advancedDupProcess.dupOperation(insn, dupType, value, kind, index));
        } else {
            callback = (value, kind, index) ->
                    DataFlowFrame.this.push(interpreter.copyOperation(insn, value));
        }
        DupType.processDuplication(
                insn, this, callback, dupType.size, dupType.shift);


    }

    @Override
    public String toString() {
        Textifier it = new Textifier();
        insnNode.accept(new TraceMethodVisitor(it));
        return ((String) it.text.get(0)).trim() + " " + super.toString();
    }
}
