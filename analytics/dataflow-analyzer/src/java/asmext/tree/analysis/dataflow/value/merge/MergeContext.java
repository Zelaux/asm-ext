package asmext.tree.analysis.dataflow.value.merge;

import lombok.SneakyThrows;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class MergeContext {
    public static final MergeContext NULL_CONTEXT = new MergeContext() {

    };

    public MergeContext() {
    }

    public MergeContext(MergeMarker<?> globalMarker, Object matchableMarker) {
        this.globalMarker = globalMarker;
        this.matchableMarker = matchableMarker;
    }

    private MergeMarker<?> globalMarker;
    private Object matchableMarker;
    private final Map<Object, ?> visitedThings = new HashMap<>();

    @SneakyThrows
    public <P, T> T visited(MergeMarker<P> marker, P value, Action<P, T> action) {


        Object prevValue = rawPut(marker, value);
        Object actualValue = prevValue;
        //noinspection rawtypes,unchecked
        if(globalMarker != null && globalMarker.match((MergeMarker) marker)) {
            actualValue = matchableMarker;
        }
        T calculate;
        try {
            //noinspection unchecked,rawtypes
            calculate = (T) ((Action) action).calculate(prevValue != null, prevValue);
        } finally {
            if(prevValue != null) rawPut(marker, prevValue);
            else visitedThings.remove(marker);
        }


        return calculate;
    }

    private @Nullable Object rawPut(Object marker, Object value) {
        //noinspection rawtypes,unchecked
        return ((Map) visitedThings).put(marker, value);
    }

    public interface Action<P, T> {
        T calculate(boolean hasValue, P param) throws Exception;
    }
}
