package asmext.tree.analysis.dataflow.value;


import asmext.tree.analysis.dataflow.util.TypeUtil;
import asmext.tree.analysis.dataflow.util.ValueUtil;
import asmext.tree.analysis.dataflow.value.merge.MergeContext;
import asmext.tree.analysis.dataflow.value.ref.SingleRef;
import asmext.tree.analysis.dataflow.value.ref.ValueRef;
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
    @EqualsAndHashCode.Exclude
    public final SingleRef myRef;
    @NotNull
    public final AbstractInsnNode producer;
    public final @NotNull ValueRef[] previousValues;
    @Getter
    public final boolean copyOp;

    CommonDataFlowValue(@NonNull Type type, @NonNull AbstractInsnNode producer, @NonNull DataFlowValue[] previousValues, boolean copyOp) {
        this(type, producer, ValueUtil.toRefs(previousValues), copyOp);
        for (@NonNull DataFlowValue v : previousValues) {
            v.addNext(this);
        }
    }

    CommonDataFlowValue(@NonNull Type type, @NonNull AbstractInsnNode producer, @NonNull ValueRef[] refs, boolean copyOp) {
        super(type);
        this.producer = producer;
        this.previousValues = refs;
        this.copyOp = copyOp;
        totalCreated++;
        myRef= (SingleRef) ValueUtil.toRef(this);
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
        ValueRef[] mergedSources = new ValueRef[size];
        boolean hasMerged = false;
        for (int i = 0; i < size; i++) {
            var oldSource = previousValues[i];
            var newSource = newCommonValue.previousValues[i];

            var deepMerge = ValueRef.mergeRef(oldSource, newSource,this);
            if (deepMerge != oldSource) hasMerged = true;
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
        mergeContext.changed();
        newCommonValue.addNext(this);
        System.arraycopy(output.previousValues, 0, previousValues, 0, output.previousValues.length);
//        previousValues=output;
        return this;
//        return future[0] = output;
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
