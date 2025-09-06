package asmext.tree.analysis.dataflow.value.merge.meta;


import asmext.tree.analysis.dataflow.interpreter.MetaDataKey;
import asmext.tree.analysis.dataflow.value.CommonDataFlowValue;

public record TmpInParentLocationMeta(int index,
                                      CommonDataFlowValue[] newValue) {
    public static final MetaDataKey<TmpInParentLocationMeta> meta = MetaDataKey.create();


}
