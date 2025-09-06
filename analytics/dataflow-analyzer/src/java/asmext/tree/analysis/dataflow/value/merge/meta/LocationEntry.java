package asmext.tree.analysis.dataflow.value.merge.meta;

import asmext.tree.analysis.dataflow.value.BaseDataFlowValue;
import asmext.tree.analysis.dataflow.value.CommonDataFlowValue;
import asmext.tree.analysis.dataflow.value.DataFlowValue;
import asmext.tree.analysis.dataflow.value.MergedDataFlowValue;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.Nullable;

/**
 * @author Zelaux
 * @since 2025-09
 */
@FieldDefaults(level = AccessLevel.PUBLIC,makeFinal = true)
public class LocationEntry {
    @Nullable
    MergedDataFlowValue[] merged;
    @Nullable
    CommonDataFlowValue[] common;
    int size;
    int index;

    public LocationEntry(@NonNull MergedDataFlowValue[] merged, int size, int index) {
        if(merged.length!=1)throw new IllegalArgumentException();
        this.merged = merged;
        this.common = null;
        this.size = size;
        this.index = index;
    }
    public LocationEntry(@NonNull CommonDataFlowValue[] common, int size, int index) {
        if(common.length!=1)throw new IllegalArgumentException();
        this.merged = null;
        this.common = common;
        this.size = size;
        this.index = index;
    }
    public void replace(DataFlowValue newValue){
        if (merged!=null) {
            newValue.addNext(merged[0]).values[index]= (BaseDataFlowValue) newValue;
        }else{
            //noinspection DataFlowIssue
            newValue.addNext(common[0]).previousValues[index]= newValue;
        }
    }
}
