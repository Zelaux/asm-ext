package com.github.asmext.tree.analysis.dataflow.value.merge;

import com.github.asmext.tree.analysis.dataflow.value.DataFlowValue;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.AbstractInsnNode;

public record IndexedNodeKey(@NotNull AbstractInsnNode producer, int size, int i) implements MergeMarker<DataFlowValue> {
    @Override
    public boolean match(MergeMarker<DataFlowValue> other) {
        if(!(other instanceof IndexedNodeKey key)) return false;
        return producer.equals(key.producer) && size== key.size && i==key.i;
    }
}
