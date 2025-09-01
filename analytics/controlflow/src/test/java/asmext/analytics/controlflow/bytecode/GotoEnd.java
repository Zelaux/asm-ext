package asmext.analytics.controlflow.bytecode;

import asmext.analytics.controlflow.TestCase;
import asmext.analytics.controlflow.methodgen.MethodGenerator;
import org.objectweb.asm.Type;

/**
 * @author Zelaux
 * @since 2025-09
 */
public class GotoEnd extends MethodGenerator {
    @Override
    public void generateInternal(Type selfType, Type owner) throws Exception {
        loadArg(0);
        loadArg(1);
        imath(ADD);
        var label = newLabel();
        goTo(label);
//
        loadArg(0);
        loadArg(1);
        imath(ADD);
        imath(ADD);

        mark(label);
        loadArg(2);
        imath(ADD);

        returnValue();
    }

    @Override
    public String[] controlFlowBlocks() {
        return new String[]{
                "range [0; 3] kind Goto",
                "range [4; 7] dead",
                "range [8; 11] kind End"
        };
    }

    @Override
    public TestCase[] cases() {
        TestCase[] cases = new TestCase[4];
        for (int i = 0; i < cases.length; i++) {
            cases[i] = new TestCase(i * 9, i * 2, i * 3, i * 4);

        }
        return cases;
    }
}
