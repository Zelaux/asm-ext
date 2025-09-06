package asmext.tree.analysis.dataflow.meta;

import asmext.tree.analysis.dataflow.interpreter.MetaDataKey;
import asmext.tree.analysis.dataflow.value.merge.meta.LocationListMeta;
import lombok.NonNull;

/**
 * @author Zelaux
 * @since 2025-09
 */
public record SelfRecursion(@NonNull LocationListMeta list) {
    public static final MetaDataKey<SelfRecursion> meta=MetaDataKey.create();

}
