package com.github.asmext.tree.analysis.dataflow.value;

import com.github.asmext.tree.analysis.dataflow.value.merge.MergeContext;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;

public class NotADataFlowValue extends CommonDataFlowValue {
    public static final Type NAV_type = Type.getObjectType("-");
    public static final NotADataFlowValue instance = new NotADataFlowValue();
    public final int hashcode;

    private NotADataFlowValue() {
        super(NAV_type, CommonDataFlowValue.NAV_LABEL_NODE, DataFlowValue.EMPTY_LIST, false);
        if(instance != null) throw new IllegalArgumentException("Cannot create NAV trice");
        hashcode = hashCode();
    }

    @Override
    public int hashCode() {
        if(instance != null) return hashcode;
        return super.hashCode();
    }

    @Override
    public @Nullable DataFlowValue deepMerge(DataFlowValue newValue, MergeContext mergeContext) {
        return newValue == this ? this : null;
    }

    @Override
    public DataFlowValue copied(AbstractInsnNode insn) {
        return this;
    }

    @Override
    public DataFlowValue merge(BaseDataFlowValue other, MergeContext mergeContext) {
        return other;
    }

    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public String toString() {
        return "<null>";
    }

    @Override
    public boolean isNull() {
        return true;
    }
}
