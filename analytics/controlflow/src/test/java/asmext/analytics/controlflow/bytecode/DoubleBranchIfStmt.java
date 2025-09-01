package asmext.analytics.controlflow.bytecode;

import asmext.analytics.controlflow.TestCase;
import asmext.analytics.controlflow.methodgen.MethodGenerator;
import org.objectweb.asm.Type;

/**
 * @author Zelaux
 * @since 2025-09
 */
public class DoubleBranchIfStmt extends MethodGenerator {
    @Override
    public void generateInternal(Type selfType, Type owner) throws Exception {
        loadArg(0);
        loadArg(1);
        imath(ADD);
        var trueBranch = newLabel();
        var finalLabel = newLabel();
        loadArg(2);
        push(4);
        ifCmp(Type.INT_TYPE, GT, trueBranch);

        loadArg(2);
        imath(ADD);
        goTo(finalLabel);

        mark(trueBranch);
        loadArg(2);
        imath(SUB);

        mark(finalLabel);

        returnValue();
    }

    @Override
    public String[] controlFlowBlocks() {
        return new String[]{
                "range [0; 5] kind IfStmt",
                "range [6; 8] kind Goto",
                "range [9; 11] kind Simple",
                "range [12; 13] kind MergeEnd"
        };
    }


    @Override
    public TestCase[] cases() {
        int[] ints = {0, 1, 2, 3, 4, 5, 6};
        TestCase[] cases = new TestCase[ints.length];
        int i = 0;
        for (int c : ints) {
            int v0 = 3;
            if (c <= 4) v0 += c;
            else v0 -= c;
            cases[i] = new TestCase(v0, 1, 2, c);
            i++;
        }
        return cases;
    }
}
