package asmext.tree.analysis.dataflow.util;

import asmext.tree.analysis.dataflow.value.BaseDataFlowValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Stream;

public class ArrayUtil {
    public static BaseDataFlowValue @NotNull [] addOne(@NotNull BaseDataFlowValue @NotNull [] array, BaseDataFlowValue value) {

        BaseDataFlowValue[] result = new BaseDataFlowValue[array.length + 1];
        System.arraycopy(array, 0, result, 0, array.length);
        result[array.length] = value;
        return result;
    }

    public static BaseDataFlowValue[] set(BaseDataFlowValue[] tmpArr1, @Nullable BaseDataFlowValue value1) {
        tmpArr1[0] = value1;
        return tmpArr1;
    }

    public static BaseDataFlowValue[] merge(BaseDataFlowValue[] a, BaseDataFlowValue[] b) {
        BaseDataFlowValue[] result = new BaseDataFlowValue[a.length + b.length];
        HashSet<BaseDataFlowValue> set = new HashSet<>();
        int i = add(a, set, result, 0);
        i = add(b, set, result, i);
        if(i == result.length) return result;
        return Arrays.copyOf(result, i);
    }

    public static BaseDataFlowValue[] simpleMerge(BaseDataFlowValue[] a, BaseDataFlowValue[] b) {
        BaseDataFlowValue[] result = new BaseDataFlowValue[a.length + b.length];

        add(result, a, 0);
        add(result, b, a.length);
        return result;
    }

    public static int add(BaseDataFlowValue[] a, HashSet<BaseDataFlowValue> set, BaseDataFlowValue[] result, int i) {
        for(BaseDataFlowValue e : a) {
            if(set.add(e)) result[i++] = e;
        }
        return i;
    }

    public static int add(BaseDataFlowValue[] result, BaseDataFlowValue[] a, int i) {
        System.arraycopy(a, 0, result, i, a.length);
        return i + a.length;
    }

    public static BaseDataFlowValue[] merge1(BaseDataFlowValue[] a, BaseDataFlowValue[] b) {
        return Stream.concat(Arrays.stream(a), Arrays.stream(b)).distinct().toArray(BaseDataFlowValue[]::new);
    }

    public static BaseDataFlowValue[] merge0(BaseDataFlowValue[] a, BaseDataFlowValue[] b) {

        BaseDataFlowValue[] result = new BaseDataFlowValue[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
}
