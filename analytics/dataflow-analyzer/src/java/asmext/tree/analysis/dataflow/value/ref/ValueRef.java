package asmext.tree.analysis.dataflow.value.ref;

import asmext.tree.analysis.dataflow.util.TypeUtil;
import asmext.tree.analysis.dataflow.value.BaseDataFlowValue;
import asmext.tree.analysis.dataflow.value.CommonDataFlowValue;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;

/**
 * @author Zelaux
 * @since 2025-09
 */

public abstract class ValueRef {
//    public abstract int getProducerIndex();

    @NotNull
    public static ValueRef mergeRef(ValueRef oldSource, ValueRef newSource, CommonDataFlowValue contextValue) {
        if (oldSource == newSource || oldSource.matches(newSource)) return oldSource;
        if (oldSource instanceof SingleRef singleRef) {
            setNext(contextValue, singleRef);
            if (newSource instanceof SingleRef newSingleRef) {
                setNext(contextValue, newSingleRef);
                return new UnionRef(TypeUtil.findUnion(singleRef.type, newSingleRef.type), UnionRef.mergeSize(singleRef.size, newSingleRef.size))
                        .withRef(singleRef)
                        .withRef(newSingleRef);
            }
            if (newSource instanceof UnionRef unionRef) return unionRef.mergeWith(singleRef);
        }
        if (oldSource instanceof UnionRef unionRef) {
            if (newSource instanceof SingleRef newSingleRef) {
                setNext(contextValue, newSingleRef);
                return unionRef.mergeWith(newSingleRef);
            }
            if (newSource instanceof UnionRef unionRef2) {
                UnionRef newUnion = new UnionRef(TypeUtil.findUnion(unionRef.type, unionRef2.type), UnionRef.mergeSize(unionRef.size, unionRef2.size));
                newUnion.refs.addAll(unionRef.refs);
                newUnion.refs.addAll(unionRef2.refs);

                return newUnion;
            }
        }
        throw new UnsupportedOperationException();
    }

    private static void setNext(CommonDataFlowValue contextValue, SingleRef newSingleRef) {
        BaseDataFlowValue baseValue = newSingleRef.baseValue;
        if (baseValue != null) baseValue.addNext(contextValue);
    }

    public abstract boolean isNull();


    public abstract boolean matches(ValueRef ref);

    public abstract Type getType();

    public abstract int getSize();
}
