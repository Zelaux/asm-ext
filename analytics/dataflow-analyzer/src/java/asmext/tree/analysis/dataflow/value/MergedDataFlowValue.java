package asmext.tree.analysis.dataflow.value;

import asmext.tree.analysis.dataflow.util.TypeUtil;
import asmext.tree.analysis.dataflow.util.ValueUtil;
import asmext.tree.analysis.dataflow.value.merge.MergeContext;
import asmext.tree.analysis.dataflow.value.ref.ValueRef;
import asmext.tree.analysis.dataflow.visitor.ValueCalculatorVisitor;
import asmext.tree.analysis.dataflow.visitor.ValueVisitor;
import lombok.NonNull;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;

import java.util.Arrays;

public final class MergedDataFlowValue extends DataFlowValue {

    public static final BaseDataFlowValue[] EMPTY_ARRAY = new BaseDataFlowValue[0];
    private static final RuntimeException breakException = new RuntimeException() {{
        setStackTrace(new StackTraceElement[]{new StackTraceElement(MergedDataFlowValue.class.getName(), "recurrent", "", 0)});
    }};
    public final int labelIndex;
    public ValueRef[] values;
    @Nullable
    public Type type;
    public int computedSize;
    MergedDataFlowValue(BaseDataFlowValue[] values, @Nullable Type type, int labelIndex) {
        this(ValueUtil.toRefs(values),type, labelIndex);
    }
    MergedDataFlowValue(@NotNull ValueRef[] refs, @Nullable Type type, int labelIndex) {
        this.values = refs;
        this.type = type;
        this.labelIndex = labelIndex;
    }

    public static DataFlowValue make(@NonNull BaseDataFlowValue oldValue, @NonNull BaseDataFlowValue newValue, int labelIndex) {
        return new MergedDataFlowValue(new BaseDataFlowValue[]{oldValue, newValue}, TypeUtil.findUnion(oldValue.type, newValue.type), labelIndex);
    }

    @Override
    public int getSize() {
        int size = 0;
        for (var value : values) {
            if (value.isNull()) continue;
            //noinspection MagicConstant
            @MagicConstant(valuesFromClass = Type.class)
            int sort = value.getType().getSort();
            switch (sort) {
                case Type.ARRAY,
                     Type.OBJECT,
                     Type.METHOD -> {
                    return 1;
                }
            }
            size = Math.max(size, value.getSize());
        }
        return size;
    }

    @Override
    public CommonDataFlowValue copied(AbstractInsnNode insn) {
        CommonDataFlowValue value;
        if (type == null) {
            value = new SizedCommonDataFlowValue(NotADataFlowValue.instance.type, insn, new DataFlowValue[]{this}, true, getSize());
        } else {
            value = new CommonDataFlowValue(type, insn, new DataFlowValue[]{this}, true);
        }
        return addNext(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MergedDataFlowValue merge(BaseDataFlowValue newValue0, MergeContext mergeContext, int labelIndex) {
        ValueRef ref = ValueUtil.toRef(newValue0);
        for (ValueRef value : values) {
            if(value.matches(ref))return this;
        }
        ValueRef[] refs = Arrays.copyOf(values, values.length + 1);
        refs[refs.length-1]=ref;
        values=refs;
        newValue0.addNext(this);
        mergeContext.changed();
        return this;
//        return new MergedDataFlowValue(refs,TypeUtil.findUnion(type,newValue0.type),labelIndex);

    }

    //region deepMerge

    @Override
    public String toString() {
        String[] strings = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            strings[i] = values[i].toString();
        }
        return "<|%d|%s|> -> [%d]".formatted(labelIndex, Arrays.toString(strings), nextNodes.size());
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public @Nullable DataFlowValue deepMerge(DataFlowValue newValue, MergeContext mergeContext) {
        DataFlowValue deepMerge;
        if (this == newValue) {
            deepMerge = this;
        } else deepMerge = null;
        return deepMerge;
    }

    //endregion


    //region visitors
    @Override
    public void accept(ValueVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T accept(ValueCalculatorVisitor<T> visitor) {
        return visitor.visit(this);
    }
    //endregion
}
