package asmext.tree.analysis.dataflow.value;

import asmext.tree.analysis.dataflow.util.ValueUtil;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * @author Zelaux
 * @since 2025-09
 */
@Getter
public class EitherMergedOrCommon {
    @Nullable
    public final MergedDataFlowValue merged;
    @Nullable
    public final CommonDataFlowValue common;

    public EitherMergedOrCommon(@NonNull MergedDataFlowValue merged) {
        this.merged = merged;
        common = null;
    }

    public EitherMergedOrCommon(@NonNull CommonDataFlowValue common) {
        this.merged = null;
        this.common = common;
    }

    public boolean isMerged() {return merged != null;}

    @Override
    public String toString() {
        DataFlowValue value = isMerged() ? merged : common;
        return value.getClass().getSimpleName() + "@" + Integer.toHexString(System.identityHashCode(value)) + " " + value.toString();
    }

    public void handle(Consumer<@NotNull MergedDataFlowValue> ifMerged, Consumer<@NotNull CommonDataFlowValue> ifCommon) {
        if (merged != null) ifMerged.accept(merged);
        else //noinspection DataFlowIssue
            ifCommon.accept(common);
    }

    public int index() {
        //noinspection DataFlowIssue
        return isMerged() ? merged.labelIndex : ValueUtil.MOCK_LIST.indexOf(common.producer);
    }
}
