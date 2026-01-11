package asmext.postprocessors.inlinefunc.classes;

import asmext.postprocessors.inlinefunc.TestInline;
import asmext.postprocessors.inlinefunc.TestTarget;

public class BooleanTestClass {
    @TestInline
    static int boolToInt(boolean x) {return x ? 1 : 0;}

    @TestTarget
    static int boolsToInt(boolean x1, boolean x2, boolean x3) {
        return boolToInt(x1) | (boolToInt(x2) << 1) | (boolToInt(x3) << 2) |((true?1:0)<<3);
    }

}
