package asmext.postprocessors.inlinefunc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class TestClass {


    static int calc2(int x) {
        return x * 2;
    }

    static int calc3(int x) {
        return x * 3;
    }



    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface InlineFunction2 {

    }

    @FunctionalInterface
    interface Int2Int {
        int calc(int x);
    }

}
