package asmext.tree.analysis.dataflow.value.ref;

import asmext.tree.analysis.dataflow.util.TypeUtil;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.objectweb.asm.Type;

import java.util.HashSet;

/**
 * @author Zelaux
 * @since 2025-09
 */
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class UnionRef extends ValueRef {

    @Getter
    public final Type type;
    @Getter
    public final int size;
    public final HashSet<SingleRef> refs = new HashSet<>(2);

    public static int mergeSize(int size1, int size2) {
        return Math.min(size1, size2);
    }

//    @Override
//    public int getProducerIndex() {
//        return labelIndex;
//    }

    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public boolean matches(ValueRef ref) {
        return ref instanceof UnionRef unionRef && refs.equals(unionRef.refs);
    }

    public UnionRef mergeWith(SingleRef singleRef) {
        if (refs.contains(singleRef)) return this;
        UnionRef newRef = new UnionRef(TypeUtil.findUnion(type,singleRef.getType()), mergeSize(size,singleRef.getSize()));
        newRef.refs.addAll(refs);
        newRef.refs.add(singleRef);
        return this;
    }

    public UnionRef withRef(SingleRef singleRef) {
        refs.add(singleRef);
        return this;
    }
}
