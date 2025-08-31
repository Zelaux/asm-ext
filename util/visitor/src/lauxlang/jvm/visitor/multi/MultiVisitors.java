package asmext.jvm.visitor.multi;

import org.jetbrains.annotations.*;
import org.objectweb.asm.*;

import java.util.function.*;

public class MultiVisitors{
    static <V1, V2> V2 visitSub(V1[] visitors, Function<V1, V2> function, IntFunction<V2[]> array, Function<V2[], V2> unifier){
        V2[] subVisitors = array.apply(visitors.length);
        int idx = 0;

        for(V1 visitor : visitors){
            V2 visitModule = function.apply(visitor);
            if(visitModule != null){
                subVisitors[idx] = visitModule;
                idx++;
            }
        }
        if(idx == 0) return null;
        if(idx < subVisitors.length){
            V2[] tmp = array.apply(idx);
            System.arraycopy(subVisitors, 0, tmp, 0, idx);
            subVisitors = tmp;
        }
        return unifier.apply(subVisitors);
    }

    public static @NotNull ClassVisitor[] sureNotNull(@Nullable ClassVisitor[] visitors){
        int idx = 0;

        for(ClassVisitor visitor : visitors){
            if(visitor != null) idx++;

        }
        if(idx == visitors.length) //noinspection NullableProblems
            return visitors;

        ClassVisitor[] classVisitors = new ClassVisitor[idx];
        idx = 0;
        for(ClassVisitor visitor : visitors){
            if(visitor == null) continue;
            classVisitors[idx] = visitor;
            idx++;
        }
        return classVisitors;
    }
}
