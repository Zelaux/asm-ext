package asmext.tree.analysis.dataflow.interpreter;

import org.objectweb.asm.tree.analysis.Value;

/**
 * @author Zelaux
 * @since 2025-09
 */
public interface InterpreterWithExtra<V extends Value> {
    void setMergeFrameIndex(int[] ref);
}
