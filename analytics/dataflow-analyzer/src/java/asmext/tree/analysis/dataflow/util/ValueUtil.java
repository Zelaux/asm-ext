package asmext.tree.analysis.dataflow.util;

import asmext.tree.analysis.dataflow.meta.DupMeta;
import asmext.tree.analysis.dataflow.value.BaseDataFlowValue;
import asmext.tree.analysis.dataflow.value.CommonDataFlowValue;
import asmext.tree.analysis.dataflow.value.DataFlowValue;
import asmext.tree.analysis.dataflow.value.MergedDataFlowValue;
import asmext.tree.analysis.dataflow.value.ref.SingleRef;
import asmext.tree.analysis.dataflow.value.ref.SpecialRef;
import asmext.tree.analysis.dataflow.value.ref.ValueRef;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InsnList;

/**
 * @author Zelaux
 * @since 2025-09
 */
public class ValueUtil {
    public static final InsnList MOCK_LIST = new InsnList();

    public static int getIndex(DataFlowValue value) {
        if (value instanceof MergedDataFlowValue merged) return merged.labelIndex;
        if (value instanceof CommonDataFlowValue common) return MOCK_LIST.indexOf(common.producer);
        if (value instanceof BaseDataFlowValue.ReturnValue) return -1;
        if (value instanceof BaseDataFlowValue.ParameterValue param) return -2 - param.idx;
        return 0;
    }

    public static @NotNull ValueRef[] toRefs(@NonNull DataFlowValue[] previousValues) {
        var refs = new ValueRef[previousValues.length];
        for (int i = 0; i < refs.length; i++) {
            refs[i] = toRef(previousValues[i]);
        }
        return refs;
    }

    public static @NotNull ValueRef toRef(DataFlowValue value) {

        if (value instanceof CommonDataFlowValue common) {
            if (common.myRef != null) return common.myRef;
            int opcode = common.producer.getOpcode();
            if (opcode == Opcodes.SWAP || common.getMeta(DupMeta.meta) != null) {
                return SpecialRef.make(common,getIndex(value), getType(value), value.getSize());
            }
            return SingleRef.make(common, getIndex(value), getType(value), value.getSize());
        }
        return SingleRef.make(value instanceof BaseDataFlowValue v ? v : null, getIndex(value), getType(value), value.getSize());
    }

    public static Type getType(DataFlowValue value) {
        if (value instanceof MergedDataFlowValue merged) return merged.type;
        if (value instanceof BaseDataFlowValue common) return common.type;
        throw new UnsupportedOperationException();
    }
}
