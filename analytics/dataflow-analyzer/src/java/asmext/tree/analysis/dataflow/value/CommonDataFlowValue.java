package asmext.tree.analysis.dataflow.value;


import asmext.tree.analysis.dataflow.util.TypeUtil;
import asmext.tree.analysis.dataflow.value.merge.IndexedNodeKey;
import asmext.tree.analysis.dataflow.value.merge.MergeContext;
import asmext.tree.analysis.dataflow.value.merge.StartMarker;
import asmext.tree.analysis.dataflow.value.merge.meta.LocationEntry;
import asmext.tree.analysis.dataflow.value.merge.meta.LocationListMeta;
import asmext.tree.analysis.dataflow.value.merge.meta.RecursiveMeta;
import asmext.tree.analysis.dataflow.value.merge.meta.TmpInParentLocationMeta;
import asmext.tree.analysis.dataflow.visitor.ValueCalculatorVisitor;
import asmext.tree.analysis.dataflow.visitor.ValueVisitor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import main.java.asmext.InsnPrint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;

@EqualsAndHashCode(callSuper = true)
public non-sealed class CommonDataFlowValue extends BaseDataFlowValue {
    public static final LabelNode NAV_LABEL_NODE = new LabelNode(new Label());
    static int totalCreated;
    @NotNull
    public final AbstractInsnNode producer;
    public final @NotNull DataFlowValue[] previousValues;
    @Getter
    public final boolean copyOp;

    CommonDataFlowValue(@NonNull Type type, @NonNull AbstractInsnNode producer, @NonNull DataFlowValue[] previousValues, boolean copyOp) {
        super(type);
        this.producer = producer;
        this.previousValues = previousValues;
        this.copyOp = copyOp;
        totalCreated++;
        for (@NonNull DataFlowValue v : previousValues) {
            if (v != null) v.addNext(this);
        }
    }

    private static int getLabelIndex(DataFlowValue newValue, DataFlowValue oldValue) {
        return oldValue instanceof MergedDataFlowValue m ? m.labelIndex : (newValue instanceof MergedDataFlowValue m ? m.labelIndex : -1);
    }

    @Override
    public String flatString() {
        return "{(%s): %s}".formatted(InsnPrint.toString(producer), type);
    }

    @Override
    public String toString() {
        return "[%d] -> {(%s): %s} -> [%d]".formatted(previousValues.length, InsnPrint.toString(producer), type, nextNodes.size());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @Nullable DataFlowValue deepMerge(DataFlowValue newValue0, MergeContext mergeContext) {

        if (!(newValue0 instanceof CommonDataFlowValue newCommonValue)) return null;
        int size = previousValues.length;
        if (newCommonValue.producer != producer || newCommonValue.previousValues.length != size) return null;
        return generateNewMerged(mergeContext, newCommonValue, size);
    }

    private @NotNull CommonDataFlowValue generateNewMerged(MergeContext mergeContext, CommonDataFlowValue newCommonValue, int size) {
        CommonDataFlowValue[] future = {this};
        DataFlowValue[] mergedSources = new DataFlowValue[size];
        boolean hasMerged = false;
        for (int i = 0; i < size; i++) {
            var oldSource = previousValues[i];
            var newSource = newCommonValue.previousValues[i];
            DataFlowValue visited = mergeContext.getVisited(new StartMarker(oldSource));

            DataFlowValue deepMerge = visited != null ? visited : oldSource.deepMerge(newSource, mergeContext);
            if (visited != null) {
                LocationListMeta.addLocation(visited, new LocationEntry(future, size, i));
            }
            if (deepMerge != oldSource) hasMerged = true;
            if (deepMerge == null) {
                deepMerge = deepMergeMerged(mergeContext, newSource, oldSource, i, size, future);
                if (deepMerge == oldSource) {
                    hasMerged = false;
                    continue;
                }
            }
            mergedSources[i] = deepMerge;
        }
        if (!hasMerged && type == newCommonValue.type) return this;

        Type union = TypeUtil.findUnion(type, newCommonValue.type);

        CommonDataFlowValue output;
        if (union != null) {
            output = new CommonDataFlowValue(union, producer, mergedSources, copyOp);
        } else {
            output = new SizedCommonDataFlowValue(
                    NotADataFlowValue.NAV_type,
                    producer,
                    mergedSources,
                    copyOp,
                    getSize()
            );
        }
        for (DataFlowValue source : mergedSources) {
            source.addNext(output);
        }

        return future[0] = output;
    }

    private DataFlowValue deepMergeMerged(MergeContext mergeContext, DataFlowValue newValue, DataFlowValue oldValue, int i, int size, CommonDataFlowValue[] future) {
        DataFlowValue deepMerge;
        final int index = i;
        //boolean visited = false;
        deepMerge = mergeContext.visited(new IndexedNodeKey(producer, size, i), oldValue, (visited, value) -> {
            if (visited) {
                //visited = false;
                value.putMeta(RecursiveMeta.meta, new RecursiveMeta(this, index, future));
                return value;
                //value = null;
            }
            //visited = true; value = oldValue;
            oldValue.putMeta(TmpInParentLocationMeta.meta, new TmpInParentLocationMeta(index, future));
            newValue.putMeta(TmpInParentLocationMeta.meta, new TmpInParentLocationMeta(index, future));
            DataFlowValue merge = mergeWithContext(oldValue, newValue, getLabelIndex(newValue, oldValue), mergeContext);
            oldValue.__removeMeta(TmpInParentLocationMeta.meta);
            newValue.__removeMeta(TmpInParentLocationMeta.meta);
            RecursiveMeta recursiveMeta = oldValue.getMeta(RecursiveMeta.meta);
            if (recursiveMeta != null) {
                assert merge != null;
                oldValue.__removeMeta(RecursiveMeta.meta);
                CommonDataFlowValue flowValue = recursiveMeta.newValue()[0];
                flowValue.previousValues[recursiveMeta.index()] = merge;
                merge.addNext(flowValue);
//                System.out.println(merge.getClass());
                merge.putMeta(RecursiveMeta.HandledRecursiveMeta.meta, new RecursiveMeta.HandledRecursiveMeta(recursiveMeta));
            }
            return merge;
        });
        return deepMerge;
    }

    @Override
    public boolean isSameSource(BaseDataFlowValue newValue) {
        return newValue instanceof CommonDataFlowValue common && common.producer == producer;
    }

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
