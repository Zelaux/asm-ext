package com.github.asmext.tree.analysis.dataflow;

import com.github.asmext.tree.analysis.dataflow.value.DataFlowValue;
import com.github.asmext.tree.analysis.dataflow.interpreter.handlers.DupOpcodeHandler;
import com.github.asmext.tree.analysis.dataflow.interpreter.handlers.PopOpcodeHandler;
import com.github.asmext.tree.analysis.dataflow.interpreter.handlers.SwapOpcodeHandler;
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

import java.lang.reflect.Field;

public class DataFlowFrame extends Frame<DataFlowValue> implements Opcodes {
    int index;
    int sourceIndex;
    public DataFlowFrame(Frame<? extends DataFlowValue> frame) {
        super(frame);
    }

    @Override
    public Frame<DataFlowValue> init(Frame<? extends DataFlowValue> frame) {
        if(frame instanceof DataFlowFrame frame1) {
            index = frame1.index;//TODO find way to save index in label frames
            sourceIndex = frame1.sourceIndex;//TODO find way to save index in label frames
            justPopped = frame1.justPopped;
        }else{
            index=-1;
            resetState();
        }
        return super.init(frame);
    }

    @Getter
    DataFlowValue[] justPopped;

    private void resetState() {
        sourceIndex=index;
        index = -1;
        justPopped = null;
    }

    public DataFlowFrame(int numLocals, int numStack) {
        super(numLocals, numStack);
        resetState();
    }

    @Override
    public boolean merge(Frame<? extends DataFlowValue> frame0, Interpreter<DataFlowValue> interpreter) throws AnalyzerException {
        DataFlowFrame newFrame = (DataFlowFrame) frame0;
        int stackSize = getStackSize();
        if(stackSize != newFrame.getStackSize()) {
            throw new AnalyzerException(null, "Incompatible stack heights");
        }
        return super.merge(newFrame, interpreter);
    }

    @Override
    public boolean merge(Frame<? extends DataFlowValue> frame, boolean[] localsUsed) {
        return super.merge(frame, localsUsed);
    }

    public static AnalyzerException illegalUse(String extraInfo, AbstractInsnNode insn) throws AnalyzerException {
        String opcode = Printer.OPCODES[Math.max(0, insn.getOpcode())];
        throw new AnalyzerException(insn, "Idllegal use of " + opcode + ": " + extraInfo);
    }

    private static AnalyzerException illegalUse(AbstractInsnNode insn) throws AnalyzerException {
        return illegalUse(insn, Printer.OPCODES[Math.max(0, insn.getOpcode())]);
    }

    private static AnalyzerException illegalUse(AbstractInsnNode insn, String pop1) throws AnalyzerException {
        throw new AnalyzerException(insn, "Illegal use of " + pop1);
    }

    @Override
    public void execute(AbstractInsnNode insn, Interpreter<DataFlowValue> interpreter) throws AnalyzerException {
        resetState();
        //noinspection MagicConstant
        @MagicConstant(valuesFromClass = Opcodes.class)
        int opcode = insn.getOpcode();
//        int wasStackSize = getStackSize();
        this.index = TMP_LIST.indexOf(insn);
//        System.out.println("StackBefore("+Printer.OPCODES[insn.getOpcode()]+"): "+getStackSize());
        try {
            switch(opcode) {
                case POP -> {
                    DataFlowValue pop = pop();
                    if(pop.getSize() == 2) throw illegalUse(insn, "POP");
                    popped(interpreter, pop, insn, 0, PopType.POP_1);
                    justPopped = new DataFlowValue[]{pop};
                }
                case POP2 -> {
                    DataFlowValue pop = pop();
                    if(pop.getSize() == 2) {
                        popped(interpreter, pop, insn, 0, PopType.POP_2);
                        justPopped = new DataFlowValue[]{pop};
                    } else {
                        DataFlowValue pop2 = pop();
                        if(pop2.getSize() != 1) throw illegalUse(insn, "POP2");
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
                    //noinspection rawtypes
                    if(interpreter instanceof SwapOpcodeHandler<?>) {
                        DataFlowValue value2 = pop();
                        DataFlowValue value1 = pop();
                        if(value1.getSize() != 1 || value2.getSize() != 1) {
                            throw illegalUse(insn);
                        }
                        //noinspection unchecked
                        var x = (SwapOpcodeHandler<DataFlowValue>) interpreter;
                        push(x.swapOpcode(insn, false, value1, value2));
                        push(x.swapOpcode(insn, true, value1, value2));
                    } else super.execute(insn, interpreter);
                }
                default -> super.execute(insn, interpreter);
            }
        } catch(Exception e) {
            System.out.println("Error In: " + Printer.OPCODES[insn.getOpcode()]);
            throw Lombok.sneakyThrow(e);
        }
    }


    private static final InsnList TMP_LIST = new InsnList();

    private final Value[] values;

    {
        try {
            Field field = Frame.class.getDeclaredField("values");
            field.setAccessible(true);
            values = (Value[]) field.get(this);
        } catch(NoSuchFieldException | IllegalAccessException e) {
            throw Lombok.sneakyThrow(e);
        }
    }

    @Override
    public DataFlowValue pop() {
        DataFlowValue pop = super.pop();
        values[getLocals() + getStackSize()] = null;
        return pop;
    }

    private void popped(Interpreter<DataFlowValue> interpreter, DataFlowValue pop, AbstractInsnNode insn, int i, PopType popType) {
        //noinspection rawtypes
        if(interpreter instanceof PopOpcodeHandler popOpcodeHandler) {
            //noinspection unchecked
            popOpcodeHandler.popped(insn, pop, popType, i);
        }
    }

    @SneakyThrows
    private void processDup(int opcode, AbstractInsnNode insn, Interpreter<DataFlowValue> interpreter) {

        DupType dupType = DupType.fromOpcode(opcode);
        assert dupType != null;
        DupType.DubCallback<DataFlowValue> callback;
        //noinspection rawtypes
        if(interpreter instanceof DupOpcodeHandler __advancedDupProcess) {
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


}
