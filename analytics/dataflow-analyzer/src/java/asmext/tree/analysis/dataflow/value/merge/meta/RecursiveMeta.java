package asmext.tree.analysis.dataflow.value.merge.meta;


import asmext.tree.analysis.dataflow.interpreter.MetaDataKey;
import asmext.tree.analysis.dataflow.value.CommonDataFlowValue;

public record RecursiveMeta(CommonDataFlowValue owner, int index,
                            CommonDataFlowValue[] newValue) {
    public static final MetaDataKey<RecursiveMeta> meta = MetaDataKey.create();

    public record HandledRecursiveMeta(CommonDataFlowValue owner, int index) {
        public static final MetaDataKey<HandledRecursiveMeta> meta = MetaDataKey.create();

        public HandledRecursiveMeta(RecursiveMeta recursiveMeta) {
            this(recursiveMeta.newValue[0], recursiveMeta.index);
        }
    }
}
