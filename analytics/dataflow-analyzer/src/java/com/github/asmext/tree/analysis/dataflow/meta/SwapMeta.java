package com.github.asmext.tree.analysis.dataflow.meta;

import com.github.asmext.tree.analysis.dataflow.interpreter.MetaDataKey;
import com.github.asmext.tree.analysis.dataflow.value.DataFlowValue;

public record SwapMeta(DataFlowValue v1, DataFlowValue v2, boolean isV1) {
    public static final MetaDataKey<SwapMeta> meta = MetaDataKey.create();

    public DataFlowValue value() {
        return isV1 ? v1 : v2;
    }
}
