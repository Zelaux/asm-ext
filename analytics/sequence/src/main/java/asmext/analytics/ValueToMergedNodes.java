package asmext.analytics;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.analysis.SourceValue;

/**
 * @author Zelaux
 * @since 2025-09
 */
public abstract class ValueToMergedNodes<V> {

    public static final ValueToMergedNodes<SourceValue> sourceValueNodes = new ValueToMergedNodes<>() {
        @Override
        public int amoutOfMerged(SourceValue value) {
            return value.insns.size();
        }

        @Override
        public void copyMergedInsnToArray(SourceValue value, AbstractInsnNode[] array) {
            value.insns.toArray(array);
        }
    };

    /**
     * returns amount of merged values in this value
     * */
    public abstract int amoutOfMerged(V value);

    public abstract void copyMergedInsnToArray(V value, AbstractInsnNode[] array);

    public void setInstructions(AbstractInsnNode[] instructions) {}
}
