package asmext.postprocessors.inlinefunc.classes;

import asmext.postprocessors.inlinefunc.TestInline;
import asmext.postprocessors.inlinefunc.TestTarget;

public class SimpleTestClass {
    static float[] arr = new float[1 << 8];

    @TestInline
    static float x(int i) {return arr[i << 1];}

    @TestInline
    static float y(int i) {return arr[(i << 1) + 1];}

    @TestTarget
    static float dot(int i, float x, float y) {
        return x(i) * x + y(i) * y;
    }

}
