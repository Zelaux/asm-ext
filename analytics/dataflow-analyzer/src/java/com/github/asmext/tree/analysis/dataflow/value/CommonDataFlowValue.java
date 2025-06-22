package com.github.asmext.tree.analysis.dataflow.value;


import com.github.asmext.tree.analysis.dataflow.meta.RecursiveMeta;
import com.github.asmext.tree.analysis.dataflow.util.TypeUtil;
import com.github.asmext.tree.analysis.dataflow.value.merge.IndexedNodeKey;
import com.github.asmext.tree.analysis.dataflow.value.merge.MergeContext;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

@EqualsAndHashCode(callSuper = true)
public non-sealed class CommonDataFlowValue extends BaseDataFlowValue {
    public static final LabelNode NAV_LABEL_NODE = new LabelNode(new Label());

    @NotNull
    public final AbstractInsnNode producer;


    public final @NotNull DataFlowValue[] previousValues;
    @Getter
    public final boolean copyOp;


    static int totalCraeted;

    CommonDataFlowValue(@NonNull Type type, @NonNull AbstractInsnNode producer, @NonNull DataFlowValue[] previousValues, boolean copyOp) {
        super(type);
        this.producer = producer;
        this.previousValues = previousValues;
        this.copyOp = copyOp;
        totalCraeted++;
    }

    @Override
    public String toString() {
        Textifier printer = new Textifier();
        TraceMethodVisitor methodVisitor = new TraceMethodVisitor(printer);
        producer.accept(methodVisitor);

        String o = (String) printer.text.get(0);
        return "{" + type + "<-- " + o.trim() + " + " + previousValues.length + "}";
    }


    public DataFlowValue copied(AbstractInsnNode insn) {
        return new CommonDataFlowValue(type, insn, new DataFlowValue[]{this}, true);
    }

    @Override
    public @Nullable DataFlowValue deepMerge(DataFlowValue newValue0, MergeContext mergeContext) {

        if(!(newValue0 instanceof CommonDataFlowValue newCommonValue)) return null;
        int size = previousValues.length;
        if(newCommonValue.producer != producer || newCommonValue.previousValues.length != size) return null;
        CommonDataFlowValue[] future = {this};
        DataFlowValue[] mergedSources = new DataFlowValue[size];
        boolean hasMerged = false;
        for(int i = 0; i < size; i++) {
            var oldSource = previousValues[i];
            var newSource = newCommonValue.previousValues[i];
            DataFlowValue deepMerge = oldSource.deepMerge(newSource, mergeContext);

            if(deepMerge != oldSource) hasMerged = true;
            if(deepMerge == null) {
                deepMerge = deepMergeMerged(mergeContext, newSource, oldSource, i, size, future);
                if(deepMerge == oldSource) {
                    hasMerged = false;
                    continue;
                }
            }
            mergedSources[i] = deepMerge;
        }
        if(!hasMerged && type == newCommonValue.type) return this;

        Type union = TypeUtil.findUnion(type, newCommonValue.type);

        CommonDataFlowValue output;
        if(union != null) {
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

        return future[0] = output;
    }

    private DataFlowValue deepMergeMerged(MergeContext mergeContext, DataFlowValue newValue, DataFlowValue merged, int i, int size, CommonDataFlowValue[] future) {
        DataFlowValue deepMerge;
        final int index = i;
        //boolean visited = false;
        deepMerge = mergeContext.visited(new IndexedNodeKey(producer, size, i), merged, (visited, value) -> {
            if(visited) {
                //visited = false;
                value.putMeta(RecursiveMeta.meta, new RecursiveMeta(this, index, future));
                return value;
                //value = null;
            }
            //visited = true; value = merged;
            DataFlowValue merge = mergeWithContext(merged, newValue, mergeContext);
            RecursiveMeta recursiveMeta = merged.getMeta(RecursiveMeta.meta);
            if(recursiveMeta != null) {
                assert merge != null;
                merged.__removeMeta(RecursiveMeta.meta);
                recursiveMeta.newValue()[0].previousValues[recursiveMeta.index()] = merge;
                merge.putMeta(RecursiveMeta.HandledRecursiveMeta.meta, new RecursiveMeta.HandledRecursiveMeta(recursiveMeta));
            }
            return merge;
        });
        return deepMerge;
    }

    @Override
    public DataFlowValue merge(BaseDataFlowValue other, MergeContext mergeContext) {
        return MergedDataFlowValue.make(this, other);
    }

    @Override
    public boolean isSameSource(BaseDataFlowValue newValue) {
        return newValue instanceof CommonDataFlowValue common && common.producer == producer;
    }


}
