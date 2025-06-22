package com.github.asmext.tree.analysis.dataflow.value;

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

    @Override
    public int getSize() {
        return size;
    }
}
