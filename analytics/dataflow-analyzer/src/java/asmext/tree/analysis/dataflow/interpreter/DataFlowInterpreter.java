package asmext.tree.analysis.dataflow.interpreter;

import asmext.tree.analysis.dataflow.DataFlowAnalyzer;
import asmext.tree.analysis.dataflow.DataFlowFrame;
import asmext.tree.analysis.dataflow.DupType;
import asmext.tree.analysis.dataflow.PopType;
import asmext.tree.analysis.dataflow.interpreter.handlers.CustomOpcodeHandler;
import asmext.tree.analysis.dataflow.interpreter.handlers.DupOpcodeHandler;
import asmext.tree.analysis.dataflow.interpreter.handlers.PopOpcodeHandler;
import asmext.tree.analysis.dataflow.interpreter.handlers.SwapOpcodeHandler;
import asmext.tree.analysis.dataflow.meta.*;
import com.github.asmext.tree.analysis.dataflow.meta.*;
import asmext.tree.analysis.dataflow.value.BaseDataFlowValue;
import asmext.tree.analysis.dataflow.value.CommonDataFlowValue;
import asmext.tree.analysis.dataflow.value.DataFlowValue;
import asmext.tree.analysis.dataflow.value.MergedDataFlowValue;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.Interpreter;

import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.tree.analysis.BasicInterpreter.NULL_TYPE;

/**
 * A custom {@link Interpreter} implementation used for data flow analysis over JVM bytecode.
 * <p>
 * This interpreter extends ASM's {@link Interpreter} and operates on {@link DataFlowValue} values,
 * tracking additional metadata for various bytecode-level operations.
 * <p>
 * The purpose of this interpreter is to analyze stack-based JVM bytecode with enhanced tracking of:
 * <ul>
 *     <li>Constants and their types ({@link ConstMeta})</li>
 *     <li>Duplication and popping behavior ({@link DupMeta}, {@link PopMeta})</li>
 *     <li>Variable load/store operations ({@link LoadMeta}, {@link StoreMeta})</li>
 *     <li>Value swaps ({@link SwapMeta})</li>
 * </ul>
 * *
 * <p>
 * This class is typically used by a {@link DataFlowAnalyzer} to perform symbolic execution of bytecode,
 * enabling optimizations, verifications, or transformations based on instruction semantics and tracked metadata.
 *
 * @author Zelaux
 * @see DataFlowValue
 * @see CommonDataFlowValue
 * @see BaseDataFlowValue.ParameterValue
 * @see BaseDataFlowValue.ReturnValue
 * @see MergedDataFlowValue
 * @see org.objectweb.asm.tree.analysis.Interpreter
 * @see org.objectweb.asm.tree.analysis.Analyzer
 */
public class DataFlowInterpreter extends Interpreter<DataFlowValue> implements Opcodes,
    DupOpcodeHandler<DataFlowValue>,
    PopOpcodeHandler<DataFlowValue>,
    SwapOpcodeHandler<DataFlowValue>,
    CustomOpcodeHandler {

    public static final Type JAVA_LANG_STRING = Type.getObjectType("java/lang/String");
    public static final Type JAVA_LANG_CLASS = Type.getObjectType("java/lang/Class");
    public static final Type JAVA_LANG_INCOKE_METHODTYPE = Type.getObjectType("java/lang/invoke/MethodType");
    public static final Type JAVA_LANG_INVOKE_METHODHANDLE = Type.getObjectType("java/lang/invoke/MethodHandle");
    private static final Type RETURNADDRESS_TYPE = Type.VOID_TYPE;
    private static final Type JAVA_LANG_OBJECT = Type.getObjectType("java/lang/Object");
    public final ArrayList<OpcodeHandler> customHandlers = new ArrayList<>();

    /**
     * Constructs a new {@link Interpreter}.
     */
    public DataFlowInterpreter() {
        super(Opcodes.ASM9);
    }

    @Override
    public DataFlowValue newOperation(AbstractInsnNode insn) throws AnalyzerException {
        ConstMeta constMeta = null;
        int opcode = insn.getOpcode();
        Type historyValueType = switch(opcode) {
            case Opcodes.ACONST_NULL -> {
                constMeta = ConstMeta.Null.instance;
                yield NULL_TYPE;
            }
            case Opcodes.ICONST_M1,
                 Opcodes.ICONST_5,
                 Opcodes.ICONST_4,
                 Opcodes.ICONST_3,
                 Opcodes.ICONST_2,
                 Opcodes.ICONST_1,
                 Opcodes.ICONST_0 -> {
                constMeta = new ConstMeta.IntegerConstant.Int(opcode - ICONST_0);
                yield Type.INT_TYPE;
            }

            case Opcodes.LCONST_0,
                 Opcodes.LCONST_1 -> {
                constMeta = new ConstMeta.IntegerConstant.Long(opcode - LCONST_0);
                yield Type.LONG_TYPE;
            }

            case Opcodes.FCONST_0,
                 Opcodes.FCONST_2,
                 Opcodes.FCONST_1 -> {
                constMeta = new ConstMeta.FloatingConstant.Float(opcode - FCONST_0);
                yield Type.FLOAT_TYPE;
            }

            case Opcodes.DCONST_0,
                 Opcodes.DCONST_1 -> {
                constMeta = new ConstMeta.FloatingConstant.Float(opcode - DCONST_0);
                yield Type.DOUBLE_TYPE;
            }

            case Opcodes.BIPUSH,
                 Opcodes.SIPUSH -> //noinspection DuplicateBranchesInSwitch

            {
                constMeta = new ConstMeta.IntegerConstant.Int(((IntInsnNode) insn).operand);
                yield Type.INT_TYPE;
            }

            case Opcodes.LDC -> {
                Object value = ((LdcInsnNode) insn).cst;
                constMeta = ConstMeta.makeFromLDC(value);
                if(value instanceof Integer) yield Type.INT_TYPE;
                if(value instanceof Float) yield Type.FLOAT_TYPE;
                if(value instanceof Long) yield Type.LONG_TYPE;
                if(value instanceof Double) yield Type.DOUBLE_TYPE;
                if(value instanceof String) yield JAVA_LANG_STRING;
                if(value instanceof Type) {
                    int sort = ((Type) value).getSort();
                    if(sort == Type.OBJECT || sort == Type.ARRAY) {
                        yield JAVA_LANG_CLASS;
                    }
                    if(sort == Type.METHOD) {
                        yield JAVA_LANG_INCOKE_METHODTYPE;
                    }
                    throw new AnalyzerException(insn, "Illegal LDC value " + value);
                }
                if(value instanceof Handle) yield JAVA_LANG_INVOKE_METHODHANDLE;
                if(value instanceof ConstantDynamic)
                    yield Type.getType(((ConstantDynamic) value).getDescriptor());
                throw new AnalyzerException(insn, "Illegal LDC value " + value);
            }
            case Opcodes.JSR -> RETURNADDRESS_TYPE;
            case Opcodes.GETSTATIC -> Type.getType(((FieldInsnNode) insn).desc);
            case Opcodes.NEW -> Type.getObjectType(((TypeInsnNode) insn).desc);
            default -> throw new IllegalStateException("Unexpected value: " + opcode);
        };
        DataFlowValue typed = DataFlowValue.typed(historyValueType, insn);
        if(constMeta != null) typed.putMeta(ConstMeta.meta, constMeta);
        return typed;
    }

    @Override
    public DataFlowValue newParameterValue(boolean isInstanceMethod, int local, Type type) {
        return DataFlowValue.parameter(type, local);
    }

    @Override
    public DataFlowValue newReturnTypeValue(Type type) {
        if(type.getSort() == Type.VOID) return null;
        return DataFlowValue.returnType(type);
    }

    @Override
    public DataFlowValue newEmptyValue(int local) {
        //        return super.newEmptyValue(local);
        return null;
    }

    @Override
    public DataFlowValue newExceptionValue(TryCatchBlockNode tryCatchBlockNode, Frame<DataFlowValue> handlerFrame, Type exceptionType) {
        return super.newExceptionValue(tryCatchBlockNode, handlerFrame, exceptionType);
    }

    @Override
    public DataFlowValue newValue(Type type) {
        throw new RuntimeException();
        //        return DataFlowValue.typed(type);
    }

    @Override
    public DataFlowValue unaryOperation(AbstractInsnNode insn, DataFlowValue value) throws AnalyzerException {
        var valueType = switch(insn.getOpcode()) {
            case INEG,
                 IINC,
                 L2I,
                 F2I,
                 D2I,
                 I2B,
                 I2C,
                 I2S -> Type.INT_TYPE;
            case FNEG,
                 I2F,
                 L2F,
                 D2F -> Type.FLOAT_TYPE;
            case LNEG,
                 I2L,
                 F2L,
                 D2L -> Type.LONG_TYPE;
            case DNEG,
                 I2D,
                 L2D,
                 F2D -> Type.DOUBLE_TYPE;
            case IFEQ,
                 IFNE,
                 IFLT,
                 IFGE,
                 IFGT,
                 IFLE,
                 TABLESWITCH,
                 LOOKUPSWITCH,
                 IRETURN,
                 LRETURN,
                 FRETURN,
                 DRETURN,
                 ARETURN,
                 PUTSTATIC -> null;
            case GETFIELD -> Type.getType(((FieldInsnNode) insn).desc);
            case NEWARRAY -> switch(((IntInsnNode) insn).operand) {
                case T_BOOLEAN -> Type.getType("[Z");
                case T_CHAR -> Type.getType("[C");
                case T_BYTE -> Type.getType("[B");
                case T_SHORT -> Type.getType("[S");
                case T_INT -> Type.getType("[I");
                case T_FLOAT -> Type.getType("[F");
                case T_DOUBLE -> Type.getType("[D");
                case T_LONG -> Type.getType("[J");
                default -> throw new AnalyzerException(insn, "Invalid array type");
            };
            case ANEWARRAY -> Type.getType("[" + Type.getObjectType(((TypeInsnNode) insn).desc));
            case ARRAYLENGTH -> //noinspection DuplicateBranchesInSwitch
                Type.INT_TYPE;
            case ATHROW -> //noinspection DuplicateBranchesInSwitch
                null;
            case CHECKCAST -> Type.getObjectType(((TypeInsnNode) insn).desc);
            case INSTANCEOF -> //noinspection DuplicateBranchesInSwitch
                Type.INT_TYPE;
            case MONITORENTER,
                 MONITOREXIT,
                 IFNULL,
                 IFNONNULL -> //noinspection DuplicateBranchesInSwitch
                null;
            default -> throw new AssertionError();
        };
        if(valueType == null) return null;
        return DataFlowValue.typed(valueType, insn, value);
    }

    @Override
    public DataFlowValue binaryOperation(AbstractInsnNode insn, DataFlowValue value1, DataFlowValue value2) throws AnalyzerException {
        Type newValueType = switch(insn.getOpcode()) {
            case IALOAD,
                 BALOAD,
                 CALOAD,
                 SALOAD,
                 IADD,
                 ISUB,
                 IMUL,
                 IDIV,
                 IREM,
                 ISHL,
                 ISHR,
                 IUSHR,
                 IAND,
                 IOR,
                 IXOR -> Type.INT_TYPE;
            case FALOAD,
                 FADD,
                 FSUB,
                 FMUL,
                 FDIV,
                 FREM -> Type.FLOAT_TYPE;
            case LALOAD,
                 LADD,
                 LSUB,
                 LMUL,
                 LDIV,
                 LREM,
                 LSHL,
                 LSHR,
                 LUSHR,
                 LAND,
                 LOR,
                 LXOR -> Type.LONG_TYPE;
            case DALOAD,
                 DADD,
                 DSUB,
                 DMUL,
                 DDIV,
                 DREM -> Type.DOUBLE_TYPE;
            case AALOAD -> JAVA_LANG_OBJECT;
            case LCMP,
                 FCMPL,
                 FCMPG,
                 DCMPL,
                 DCMPG -> Type.INT_TYPE;
            case IF_ICMPEQ,
                 IF_ICMPNE,
                 IF_ICMPLT,
                 IF_ICMPGE,
                 IF_ICMPGT,
                 IF_ICMPLE,
                 IF_ACMPEQ,
                 IF_ACMPNE,
                 PUTFIELD -> null;
            default -> throw new AssertionError();
        };
        if(newValueType == null) return null;
        return DataFlowValue.typed(newValueType, insn, value1, value2);
    }

    @Override
    public DataFlowValue ternaryOperation(AbstractInsnNode insn, DataFlowValue value1, DataFlowValue value2, DataFlowValue value3) throws AnalyzerException {
        return null;
    }

    @Override
    public DataFlowValue naryOperation(AbstractInsnNode insn, List<? extends DataFlowValue> values) throws AnalyzerException {
        int opcode = insn.getOpcode();
        Type type = switch(opcode) {
            case MULTIANEWARRAY -> Type.getType(((MultiANewArrayInsnNode) insn).desc);
            case INVOKEDYNAMIC -> Type.getReturnType(((InvokeDynamicInsnNode) insn).desc);
            default -> Type.getReturnType(((MethodInsnNode) insn).desc);
        };
        return DataFlowValue.typed(type, insn, values.toArray(DataFlowValue[]::new));
    }

    @Override
    public void returnOperation(AbstractInsnNode insn, DataFlowValue value, DataFlowValue expected) throws AnalyzerException {

        // Nothing to do.
    }

    @Override
    @NotNull
    public DataFlowValue merge(DataFlowValue value1, DataFlowValue value2) {

        DataFlowValue dataFlowValue = DataFlowValue.mergeValuesFromDifferentBranches(value1, value2);
        return DataFlowValue.nonNullOrNAV(dataFlowValue);
    }

    @Override
    public DataFlowValue dupOperation(AbstractInsnNode insn, DupType dupType, DataFlowValue value, DupType.ObjectKind kind, int index) throws AnalyzerException {
        DataFlowValue dataFlowValue = copyOperation(insn, value);
        dataFlowValue.putMeta(DupMeta.meta, new DupMeta(dupType, kind, index));
        return dataFlowValue;
    }

    @Override
    public CommonDataFlowValue copyOperation(AbstractInsnNode insn, DataFlowValue value) throws AnalyzerException {

        CommonDataFlowValue copied = value.copied(insn);
        int opcode = insn.getOpcode();
        if(ILOAD <= opcode && opcode <= ALOAD) {
            VarInsnNode varInsnNode = (VarInsnNode) insn;
            copied.putMeta(LoadMeta.meta, new LoadMeta(varInsnNode, varInsnNode.var));
        }
        if(ISTORE <= opcode && opcode <= ASTORE) {
            VarInsnNode varInsnNode = (VarInsnNode) insn;
            copied.putMeta(StoreMeta.meta, new StoreMeta(varInsnNode, varInsnNode.var));
        }
        return copied;
    }

    @Override
    public void popped(AbstractInsnNode insn, DataFlowValue pop, PopType popType, int i) {
        pop.putMeta(PopMeta.meta, new PopMeta((InsnNode) insn, popType, i));
    }

    @Override
    public SwapResult<DataFlowValue> handleSwapOpcode(AbstractInsnNode swapInsn, DataFlowValue bottomValue, DataFlowValue topValue, @NotNull SwapResult<DataFlowValue> result) throws AnalyzerException {
        CommonDataFlowValue newTop = copyOperation(swapInsn, bottomValue);
        CommonDataFlowValue newBottom = copyOperation(swapInsn, topValue);
        newTop.putMeta(SwapMeta.meta, new SwapMeta(newBottom, newTop, true));
        newBottom.putMeta(SwapMeta.meta, new SwapMeta(newBottom, newTop, false));

        return result.set(newBottom, newTop);
    }


    public DataFlowInterpreter customHandler(OpcodeHandler handler) {
        customHandlers.add(handler);
        return this;
    }

    @Override
    public void beforeOperation(DataFlowFrame frame, AbstractInsnNode insn) {
        if(customHandlers.isEmpty()) return;
        for(OpcodeHandler customHandler : customHandlers) {
            customHandler.beforeOperation(this, frame, insn);
        }
    }

    @Override
    public void afterOperation(DataFlowFrame frame, AbstractInsnNode insn) {
        if(customHandlers.isEmpty()) return;

        for(OpcodeHandler customHandler : customHandlers) {
            customHandler.afterOperation(this, frame, insn);
        }
    }

    @Override
    public void beforeMerge(DataFlowFrame sourceFrame, Frame<? extends DataFlowValue> frame, int index) {

        if(customHandlers.isEmpty()) return;

        for(OpcodeHandler customHandler : customHandlers) {
            customHandler.beforeMerge(this,sourceFrame, (DataFlowFrame)frame, index);
        }
    }

    @Override
    public void afterMerge(DataFlowFrame sourceFrame, Frame<? extends DataFlowValue> frame, int index, boolean changed) {

        if(customHandlers.isEmpty()) return;

        for(OpcodeHandler customHandler : customHandlers) {
            customHandler.afterMerge(this,sourceFrame, (DataFlowFrame)frame, index,changed);
        }
    }
}
