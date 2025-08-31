package asmext.tree.analysis.dataflow.util;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

public class TypeUtil {
    public static @Nullable Type findUnion(@Nullable Type typeA, @Nullable Type typeB) {
        if(typeA == null) return typeB;
        if(typeB == null) return typeA;
        if(typeA.equals(typeB)) return typeA;
        return null;
//        return null;
    }
}
