package com.github.asmext.tree.analysis.dataflow.value;

import com.github.asmext.tree.analysis.dataflow.util.ArrayUtil;
import com.github.asmext.tree.analysis.dataflow.util.TypeUtil;
import com.github.asmext.tree.analysis.dataflow.value.merge.MergeContext;
import lombok.NonNull;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;

import java.util.Arrays;

public final class MergedDataFlowValue extends DataFlowValue {

    public BaseDataFlowValue[] values;
    @Nullable
    public Type type;

    MergedDataFlowValue(BaseDataFlowValue[] values, @Nullable Type type) {
        this.values = values;
        this.type = type;
    }

    public static DataFlowValue make(@NonNull BaseDataFlowValue oldValue, @NonNull BaseDataFlowValue newValue) {
        return new MergedDataFlowValue(new BaseDataFlowValue[]{oldValue, newValue}, TypeUtil.findUnion(oldValue.type, newValue.type));
    }

    @Override
    public int getSize() {
        int size = 0;
        for(BaseDataFlowValue value : values) {
            if(value.isNull()) continue;
            //noinspection MagicConstant
            @MagicConstant(valuesFromClass = Type.class)
            int sort = value.type.getSort();
            switch(sort) {
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
    public DataFlowValue copied(AbstractInsnNode insn) {
        if(type == null)
            return new SizedCommonDataFlowValue(NotADataFlowValue.instance.type, insn, new DataFlowValue[]{this}, true, getSize());
        return new CommonDataFlowValue(type, insn, new DataFlowValue[]{this}, true);
    }

    @Override
    public MergedDataFlowValue merge(BaseDataFlowValue newValue, MergeContext mergeContext) {
        deepMergeMarker++;
        MergedDataFlowValue oldValue = this;

        Type unionType = TypeUtil.findUnion(type, newValue.type);

        BaseDataFlowValue[] values;
        // x =                                                 param(0)
        // x =                                                 param(0) OR foo(x)
        // x =                         param(0) OR [foo(x) <- (param(0) OR foo(x))]
        // x = param(0) OR [foo(x) <- [param(0) OR [foo(x) <- (param(0) OR foo(x))]] )]
        //
        //
        //         SELF =           param(0) OR ((x++) <- SELF)
        //(x++) <- SELF = (x++) <- (param(0) OR ((x++) <- SELF))
        @NotNull
        BaseDataFlowValue[] oldValues = oldValue.values;

        BaseDataFlowValue[] mergedValues = addValue(oldValues, newValue, mergeContext);


        //noinspection ReplaceNullCheck
        if(mergedValues != null) {
            values = mergedValues;
        } else {
            values = ArrayUtil.addOne(oldValues, newValue);
        }
        deepMergeMarker--;
        if(values==this.values)return this;


        return new MergedDataFlowValue(values, unionType);
    }



    private static BaseDataFlowValue @Nullable [] addValue(@NotNull BaseDataFlowValue[] oldValues, BaseDataFlowValue newValue, MergeContext mergeContext) {
        BaseDataFlowValue[] mergedValues = null;
        boolean isSame = true;
        for(int i = 0; i < oldValues.length; i++) {
            BaseDataFlowValue oldValue = oldValues[i];
            if(oldValue.isSameSource(newValue)) {

                DataFlowValue deepMerge = oldValue.deepMerge(newValue, mergeContext);
                if(deepMerge == null) {
                    throw new IllegalArgumentException("Values from same source cannot be unmegable: \n\tvalue1: " + oldValue + "\n\tvalue2: " + newValue);
                }
                if(deepMerge == oldValue) continue;
                isSame = false;
                if(deepMerge instanceof BaseDataFlowValue baseMergedValue) {
                    mergedValues = Arrays.copyOf(oldValues, oldValues.length);
                    mergedValues[i] = baseMergedValue;
                    break;
                }
                throw new IllegalArgumentException(
                    "Base Values from same source cannot be deepMerged into MergedDataFlowValue: " +
                    "\n\tvalue1: " + oldValue +
                    "\n\tvalue2: " + newValue
                );
            }
        }
        return isSame ? oldValues : mergedValues;
    }


    private BaseDataFlowValue deepSearch(BaseDataFlowValue source, MergedDataFlowValue target) {
        if(source instanceof BaseDataFlowValue.ParameterValue) return null;
        if(source instanceof BaseDataFlowValue.ReturnValue) return null;
        CommonDataFlowValue common = (CommonDataFlowValue) source;
        for(@NotNull DataFlowValue value : common.previousValues) {
            if(!(value instanceof BaseDataFlowValue base)) continue;
            BaseDataFlowValue found = deepSearch(base, target);
            if(found != null) return found;
        }
        return null;
    }


    @Override
    public String toString() {
        return "<|" + Arrays.toString(values) + "|>";
    }

    //region deepMerge

    private static final RuntimeException breakException = new RuntimeException() {{
        setStackTrace(new StackTraceElement[]{new StackTraceElement(MergedDataFlowValue.class.getName(), "recurrent", "", 0)});
    }};
    private int deepMergeMarker = 0;

    @Override
    public @Nullable DataFlowValue deepMerge(DataFlowValue newValue, MergeContext mergeContext) {

        deepMergeMarker++;
        DataFlowValue deepMerge;
        if(this == newValue) {
            deepMerge = this;
        } else deepMerge = null;
        deepMergeMarker--;
        return deepMerge;
    }

    //endregion
}
