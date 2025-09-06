package asmext.tree.analysis.dataflow.util;

import asmext.tree.analysis.dataflow.value.EitherMergedOrCommon;
import lombok.AllArgsConstructor;

/**
 * @author Zelaux
 * @since 2025-09
 */
@AllArgsConstructor
public class ValueEntry {
    public final EitherMergedOrCommon value;

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ValueEntry)) return false;
        return obj.hashCode() == hashCode();
    }

    @Override
    public int hashCode() {
        return value.index();
    }
}
