package asmext.tree.analysis.dataflow.value;

import asmext.tree.analysis.dataflow.value.ref.ValueRef;
import asmext.tree.analysis.dataflow.visitor.ValueCalculatorVisitor;
import asmext.tree.analysis.dataflow.visitor.ValueVisitor;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;

@EqualsAndHashCode(callSuper = true)
public class SizedCommonDataFlowValue extends CommonDataFlowValue {
    public int size;
     SizedCommonDataFlowValue(@NonNull Type type, @NonNull AbstractInsnNode producer, @NotNull @NonNull DataFlowValue[] previousValues, boolean copyOp, int size) {
        super(type, producer, previousValues, copyOp);
        this.size = size;
    }

    SizedCommonDataFlowValue(@NonNull Type type, @NonNull AbstractInsnNode producer, @NotNull @NonNull ValueRef[] refs, boolean copyOp,int size) {
        super(type, producer, refs, copyOp);
        this.size=size;
    }

    @Override
    public int getSize() {
        return size;
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
