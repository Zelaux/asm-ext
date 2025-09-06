package asmext.tree.analysis.dataflow.value;

import asmext.tree.analysis.dataflow.util.ArrayUtil;
import asmext.tree.analysis.dataflow.util.TypeUtil;
import asmext.tree.analysis.dataflow.value.merge.IndexedMergePartKey;
import asmext.tree.analysis.dataflow.value.merge.MergeContext;
import asmext.tree.analysis.dataflow.value.merge.StartMarker;
import asmext.tree.analysis.dataflow.value.merge.meta.LocationEntry;
import asmext.tree.analysis.dataflow.value.merge.meta.LocationListMeta;
import asmext.tree.analysis.dataflow.value.merge.meta.TmpInParentLocationMeta;
import asmext.tree.analysis.dataflow.visitor.ValueCalculatorVisitor;
import asmext.tree.analysis.dataflow.visitor.ValueVisitor;
import lombok.NonNull;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Function;

public final class MergedDataFlowValue extends DataFlowValue {

    public static final BaseDataFlowValue[] EMPTY_ARRAY = new BaseDataFlowValue[0];
    private static final RuntimeException breakException = new RuntimeException() {{
        setStackTrace(new StackTraceElement[]{new StackTraceElement(MergedDataFlowValue.class.getName(), "recurrent", "", 0)});
    }};
    public final int labelIndex;
    public BaseDataFlowValue[] values;
    @Nullable
    public Type type;
    private int deepMergeMarker = 0;

    MergedDataFlowValue(BaseDataFlowValue[] values, @Nullable Type type, int labelIndex) {
        this.values = values;
        this.type = type;
        this.labelIndex = labelIndex;
    }

    public static DataFlowValue make(@NonNull BaseDataFlowValue oldValue, @NonNull BaseDataFlowValue newValue, int labelIndex) {
        return new MergedDataFlowValue(new BaseDataFlowValue[]{oldValue, newValue}, TypeUtil.findUnion(oldValue.type, newValue.type), labelIndex);
    }

    private static BaseDataFlowValue @Nullable [] addValue(@NotNull BaseDataFlowValue[] oldValues, BaseDataFlowValue newValue, MergeContext mergeContext, @NonNull MergedDataFlowValue[] reference) {
        BaseDataFlowValue[] mergedValues = null;
        boolean isSame = true;
        int i=0;
        for (; i < oldValues.length; i++) {
            BaseDataFlowValue oldValue = oldValues[i];
            if (oldValue.isSameSource(newValue) && oldValue != newValue) {
//                mergeContext.visited(new IndexedNodeKey(this,oldValue.getSize(),i),)
                DataFlowValue visited = mergeContext.getVisited(new StartMarker(oldValue));
                DataFlowValue deepMerge = visited != null ? visited : oldValue.deepMerge(newValue, mergeContext);
                if (deepMerge == null) {
                    throw new IllegalArgumentException("Values from same source cannot be unmegable: \n\tvalue1: " + oldValue + "\n\tvalue2: " + newValue);
                }
                if (visited != null) {
                    LocationListMeta.addLocation(visited, new LocationEntry(reference, oldValues.length, i));
                }
                if (deepMerge == oldValue) continue;
                isSame = false;
                if (deepMerge instanceof BaseDataFlowValue baseMergedValue) {
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
        /*for (; i < oldValues.length; i++) {
            mergeContext.getVisited()
            isSame = false;
            if () {
                mergedValues = Arrays.copyOf(oldValues, oldValues.length);
                mergedValues[i] = baseMergedValue;
                break;
            }
        }*/
        return isSame ? oldValues : mergedValues;
    }

    @Override
    public int getSize() {
        int size = 0;
        for (BaseDataFlowValue value : values) {
            if (value.isNull()) continue;
            //noinspection MagicConstant
            @MagicConstant(valuesFromClass = Type.class)
            int sort = value.type.getSort();
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
        deepMergeMarker++;


        MergedDataFlowValue oldValue = this;
        //noinspection unchecked
        ArrayDeque<BaseDataFlowValue> queue = new ArrayDeque<>();
        queue.add(newValue0);
        ArrayList<TmpInParentLocationMeta> places = new ArrayList<>();
        MergedDataFlowValue[] reference = new MergedDataFlowValue[1];
        while (!queue.isEmpty()) {
            final BaseDataFlowValue newValue = queue.pop();
            reference[0] = this;
            if (!(newValue instanceof CommonDataFlowValue)) {
                oldValue = processAddValue(mergeContext, newValue, this, reference);
                continue;
            }
            final var curOldValue = oldValue;
            Function<MergedDataFlowValue, MergedDataFlowValue> callback = other -> {
                TmpInParentLocationMeta meta = other.getMeta(TmpInParentLocationMeta.meta);
                if (meta != null) places.add(meta);

                queue.addAll(Arrays.asList(other.values));
                return curOldValue;
            };
            oldValue = mergeContext.visited(new IndexedMergePartKey(labelIndex), callback, (visited, duplicatedGetter) -> {
                if (visited) {
                    return duplicatedGetter.apply(curOldValue);
                }

                return processAddValue(mergeContext, newValue, curOldValue, reference);
            });
            if (oldValue == curOldValue) break;
            for (TmpInParentLocationMeta place : places) {
                CommonDataFlowValue v = place.newValue()[0];
                v.previousValues[place.index()] = oldValue;
                oldValue.addNext(v);
            }
            places.clear();
        }


        return oldValue;

    }

    private @NotNull MergedDataFlowValue processAddValue(MergeContext mergeContext, BaseDataFlowValue newValue, MergedDataFlowValue curOldValue, @NonNull MergedDataFlowValue[] reference) {
        Type unionType = TypeUtil.findUnion(type, newValue.type);

        BaseDataFlowValue[] values;
        @NotNull
        BaseDataFlowValue[] oldValues = curOldValue.values;

        BaseDataFlowValue[] mergedValues = addValue(oldValues, newValue, mergeContext, reference);

        //noinspection ReplaceNullCheck
        if (mergedValues != null) {
            values = mergedValues;
        } else {
            values = ArrayUtil.addOne(oldValues, newValue);
        }
        deepMergeMarker--;
        if (values == this.values) return this;

        return reference[0] = new MergedDataFlowValue(values, unionType, labelIndex);
    }

    //region deepMerge

    private BaseDataFlowValue deepSearch(BaseDataFlowValue source, MergedDataFlowValue target) {
        if (source instanceof BaseDataFlowValue.ParameterValue) return null;
        if (source instanceof BaseDataFlowValue.ReturnValue) return null;
        CommonDataFlowValue common = (CommonDataFlowValue) source;
        for (@NotNull DataFlowValue value : common.previousValues) {
            if (!(value instanceof BaseDataFlowValue base)) continue;
            BaseDataFlowValue found = deepSearch(base, target);
            if (found != null) return found;
        }
        return null;
    }

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

        deepMergeMarker++;
        DataFlowValue deepMerge;
        if (this == newValue) {
            deepMerge = this;
        } else deepMerge = null;
        deepMergeMarker--;
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
