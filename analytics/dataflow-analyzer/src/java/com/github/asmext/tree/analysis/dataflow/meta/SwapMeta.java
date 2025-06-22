package com.github.asmext.tree.analysis.dataflow.meta;

import com.github.asmext.tree.analysis.dataflow.interpreter.MetaDataKey;
import com.github.asmext.tree.analysis.dataflow.value.CommonDataFlowValue;
import com.github.asmext.tree.analysis.dataflow.value.DataFlowValue;

/**
 * Represents values after swapping
 */
public record SwapMeta(CommonDataFlowValue bottomValue, CommonDataFlowValue topValue, boolean isTopAfterSwap) {
    public static final MetaDataKey<SwapMeta> meta = MetaDataKey.create();


    public DataFlowValue afterSwapSelf() {
        return isTopAfterSwap ? topValue : bottomValue;
    }

    public DataFlowValue afterSwapSibling() {
        return !isTopAfterSwap ? topValue : bottomValue;
    }

}
