package asmext.tree.analysis.dataflow.value.merge.meta;

import asmext.tree.analysis.dataflow.util.ValueUtil;
import asmext.tree.analysis.dataflow.value.BaseDataFlowValue;
import asmext.tree.analysis.dataflow.value.CommonDataFlowValue;
import asmext.tree.analysis.dataflow.value.DataFlowValue;
import asmext.tree.analysis.dataflow.value.MergedDataFlowValue;
import lombok.AccessLevel;
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
    CommonDataFlowValue[] common;
    int size;
    int index;

    public LocationEntry(@NonNull CommonDataFlowValue[] common, int size, int index) {
        if(common.length!=1)throw new IllegalArgumentException();
        this.common = common;
        this.size = size;
        this.index = index;
    }
    public void replace(DataFlowValue newValue){
        newValue.addNext(common[0]).previousValues[index]= ValueUtil.toRef(newValue);
    }
}
