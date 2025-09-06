package asmext.tree.analysis.dataflow.value.merge;

import asmext.tree.analysis.dataflow.util.ValueUtil;
import asmext.tree.analysis.dataflow.value.CommonDataFlowValue;
import asmext.tree.analysis.dataflow.value.DataFlowValue;

/**
 * Uses when merging {@link CommonDataFlowValue}
 * @author Zelaux
 * @since 2025-09
 */
public record StartMarker(int insnIndex)  implements MergeMarker<DataFlowValue>{
    public StartMarker(DataFlowValue value) {
        this(ValueUtil.getIndex(value));
    }

    @Override
    public boolean match(MergeMarker<DataFlowValue> other) {
        return other instanceof StartMarker marker && marker.insnIndex==insnIndex;
    }
}
