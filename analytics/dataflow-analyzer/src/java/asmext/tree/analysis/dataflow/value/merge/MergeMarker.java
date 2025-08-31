package asmext.tree.analysis.dataflow.value.merge;

public interface MergeMarker<T> {
     boolean match(MergeMarker<T> other);
}
