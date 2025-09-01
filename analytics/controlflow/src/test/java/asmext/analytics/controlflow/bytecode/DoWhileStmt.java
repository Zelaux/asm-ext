package asmext.analytics.controlflow.bytecode;

import asmext.analytics.controlflow.TestCase;
import asmext.analytics.controlflow.methodgen.MethodGenerator;
import org.objectweb.asm.Type;

/**
 * @author Zelaux
 * @since 2025-09
 */
public class DoWhileStmt extends MethodGenerator {
    @Override
    public void generateInternal(Type selfType, Type owner) throws Exception {

        loadArg(0);

        var loopStart = mark();

        loadArg(1);
        imath(ADD);

        dup();
        loadArg(2);
        ifCmp(Type.INT_TYPE, LE, loopStart);

        returnValue();
    }

    @Override
    public String[] controlFlowBlocks() {
        return new String[]{
                "range [0; 0] kind Simple",
                "range [1; 6] kind MergeIfLoop"
        };
    }


    @Override
    public TestCase[] cases() {
        int[] ints = {1, 2, 3, 4, 5, 6};
        TestCase[] cases = new TestCase[ints.length];
        int i = 0;
        for (int c : ints) {
            int v0 = 1;

            do {
                v0 += c;
            } while (v0 <= 8);
            cases[i] = new TestCase(v0, 1, c, 8);
            i++;
        }
        return cases;
    }
}
