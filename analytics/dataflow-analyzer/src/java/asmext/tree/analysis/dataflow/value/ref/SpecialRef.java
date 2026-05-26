package asmext.tree.analysis.dataflow.value.ref;

import asmext.tree.analysis.dataflow.value.BaseDataFlowValue;
import org.objectweb.asm.Type;

/**
 * @author Zelaux
 * @since 2025-09
 */
public class SpecialRef extends SingleRef {
    SpecialRef(BaseDataFlowValue baseValue, int producerIndex, Type type, int size) {
        super(baseValue, producerIndex, type, size);
    }

    public static SpecialRef make(BaseDataFlowValue baseValue, int index, Type type, int size) {
        return new SpecialRef(baseValue, index, type, size);
    }
}
