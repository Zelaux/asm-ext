package asmext.tree.analysis.dataflow.value.ref;

import asmext.tree.analysis.dataflow.value.BaseDataFlowValue;
import asmext.tree.analysis.dataflow.value.CommonDataFlowValue;
import lombok.*;
import org.objectweb.asm.Type;

/**
 * @author Zelaux
 * @since 2025-09
 */
@EqualsAndHashCode
@ToString
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class SingleRef extends ValueRef {
    @Getter
    @EqualsAndHashCode.Exclude
    public final BaseDataFlowValue baseValue;
    @Getter
    public final int producerIndex;
    @Getter
    public final Type type;
    @Getter
    public final int size;

    public static ValueRef make(BaseDataFlowValue baseValue,int index, Type type, int size) {
        return new SingleRef(baseValue,index, type, size);
    }


    @Override
    public boolean isNull() {
        return size == 0;
    }

    @Override
    public boolean matches(ValueRef ref) {
        return (ref instanceof SingleRef other) && producerIndex == other.producerIndex;
    }

}
