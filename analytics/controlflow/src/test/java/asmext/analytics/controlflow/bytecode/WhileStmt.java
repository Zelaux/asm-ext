package asmext.analytics.controlflow.bytecode;

import asmext.analytics.controlflow.TestCase;
import asmext.analytics.controlflow.methodgen.MethodGenerator;
import org.objectweb.asm.Type;

/**
 * @author Zelaux
 * @since 2025-09
 */
public class WhileStmt extends MethodGenerator {
    @Override
    public void generateInternal(Type selfType, Type owner) throws Exception {

        loadArg(0);

        var outOfLoop = newLabel();
        var startMark = mark();

        dup();
        loadArg(2);
        ifCmp(Type.INT_TYPE, GT, outOfLoop);

        loadArg(1);
        imath(ADD);
        goTo(startMark);

        mark(outOfLoop);

        returnValue();
    }

    @Override
    public String[] controlFlowBlocks() {
        return new String[]{
                "range [0; 0] kind Simple",
                "range [1; 4] kind MergeIf",
                "range [5; 7] kind GotoLoop",
                "range [8; 9] kind End"
        };
    }


    @Override
    public TestCase[] cases() {
        int[] ints = {1, 2, 3, 4, 5, 6};
        TestCase[] cases = new TestCase[ints.length];
        int i = 0;
        for (int c : ints) {
            int v0 = 1;
            while (v0 <= 8) {
                v0 += c;
            }
            cases[i] = new TestCase(v0, 1, c, 8);
            i++;
        }
        return cases;
    }
}
