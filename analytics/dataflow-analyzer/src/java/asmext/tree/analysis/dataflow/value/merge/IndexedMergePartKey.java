package asmext.tree.analysis.dataflow.value.merge;

import asmext.tree.analysis.dataflow.value.MergedDataFlowValue;

import java.util.function.Function;

public record IndexedMergePartKey(int labelIndex) implements MergeMarker<Function<MergedDataFlowValue, MergedDataFlowValue>> {
    @Override
    public boolean match(MergeMarker<Function<MergedDataFlowValue, MergedDataFlowValue>> other) {
        if(!(other instanceof IndexedMergePartKey key)) return false;
        return labelIndex == key.labelIndex;
    }
}
