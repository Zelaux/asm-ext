package asmext.tree.analysis.dataflow.value;

import asmext.tree.analysis.dataflow.interpreter.MetaDataKey;
import asmext.tree.analysis.dataflow.value.merge.MergeContext;
import asmext.tree.analysis.dataflow.visitor.ValueCalculatorVisitor;
import asmext.tree.analysis.dataflow.visitor.ValueVisitor;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.analysis.Value;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public sealed abstract class DataFlowValue implements Value
    permits BaseDataFlowValue, MergedDataFlowValue {

    public static final DataFlowValue[] EMPTY_LIST = new DataFlowValue[0];

    //region makers

    public static DataFlowValue parameter(@NonNull Type type, int idx) {
        return BaseDataFlowValue.parameter(type, idx);
    }

    public static DataFlowValue typed(@NonNull Type type, AbstractInsnNode producer, DataFlowValue... from) {
        return BaseDataFlowValue.typed(type, producer, from);
    }

    public static DataFlowValue returnType(@NonNull Type type) {
        return BaseDataFlowValue.returnType(type);
    }

    //endregion


    @NotNull
    public static DataFlowValue nonNullOrNAV(@Nullable DataFlowValue value) {
        return value == null ? NotADataFlowValue.instance : value;
    }

    public static boolean isNull(@Nullable DataFlowValue value2) {
        return value2 == null || value2.isNull();
    }

    public boolean isNull() {
        return false;
    }


    @Nullable
    public static DataFlowValue mergeValuesFromDifferentBranches(@Nullable DataFlowValue oldValue, @Nullable DataFlowValue newValue) {
        return mergeWithContext(oldValue, newValue, new MergeContext());
    }

    public static @Nullable DataFlowValue mergeWithContext(@Nullable DataFlowValue oldValue, @Nullable DataFlowValue newValue, MergeContext mergeContext) {
                boolean null1 = isNull(oldValue);
        boolean null2 = isNull(newValue);
        if(null2||null1) return null;

        DataFlowValue deepMerge = oldValue.deepMerge(newValue, mergeContext);
        if(deepMerge != null) return deepMerge;
        if(newValue instanceof BaseDataFlowValue newBaseValue) return oldValue.merge(newBaseValue, mergeContext);


        if(oldValue instanceof BaseDataFlowValue base && newValue instanceof MergedDataFlowValue merged) {
            return merged.merge(base, mergeContext);
        }
        if(oldValue instanceof MergedDataFlowValue oldMerged && newValue instanceof MergedDataFlowValue newMerged) {
            DataFlowValue curr = oldMerged;
            for(BaseDataFlowValue value : newMerged.values) {
                curr = mergeWithContext(curr, value, mergeContext);
            }
            return curr;
        }

        throw new UnsupportedOperationException("newValue is unsupported to be non Base");
    }


    //region <editor-fold desc="Meta">

    @Nullable
    private Map<MetaDataKey<?>, ?> metaData;

    public <T> T getMeta(@NotNull MetaDataKey<T> key) {
        if(metaData == null) return null;
        //noinspection unchecked
        return (T) metaData.get(key);
    }

    public <T> void putMeta(@NotNull MetaDataKey<T> key, T value) {
        Map<MetaDataKey<?>, ?> map = metaData;
        if(map == null) map = metaData = new HashMap<>();
        //noinspection unchecked,rawtypes
        ((Map) map).put(key, value);
    }

    protected void __removeMeta(@SuppressWarnings("SameParameterValue") MetaDataKey<?> key) {
        if(metaData == null) return;
        metaData.remove(key);
    }

    public Stream<? extends Map.Entry<MetaDataKey<?>, ?>> getMetaEntries() {
        if(metaData == null) return Stream.empty();
        return metaData.entrySet().stream();
    }

    public int metaDataSize() {
        return metaData == null ? 0 : metaData.size();
    }
    //endregion

    @Override
    public abstract int getSize();

    public abstract CommonDataFlowValue copied(AbstractInsnNode insn);

    /**
     * <p>Attempts to deeply merge this value with another value.</p>
     *
     * <p>The method implements the following behavior:</p>
     * <ul>
     *   <li>Returns {@code this} instance if it is <em>identical</em> to {@code newValue}</li>
     *   <li>Returns {@code null} if the values cannot be merged</li>
     *   <li>Subclasses may implement additional merging logic</li>
     * </ul>
     *
     * <p><b>Usage example:</b></p>
     * <pre>{@code
     * Value merged = original.deepMerge(newValue);
     * if (merged == null) {
     *     // Handle unmergeable case
     * }
     * }</pre>
     *
     * @param newValue     the value to merge with this value
     * @param mergeContext context that helps to keep track of visited variables
     * @return {@code this} if identical, a merged result if successful, otherwise {@code null}
     */
    @Nullable
    public abstract DataFlowValue deepMerge(DataFlowValue newValue, MergeContext mergeContext);

    public abstract DataFlowValue merge(BaseDataFlowValue newValue, MergeContext mergeContext);


    //region Visitors
    public abstract void accept(ValueVisitor visitor);
    public abstract<T> T accept(ValueCalculatorVisitor<T> visitor);
    //endregion
}
