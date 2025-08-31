package asmext.util.dispatch;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GenerateInExistenClassTest {
    @Test
    void testCanRun() {
        String[] args = new String[1];
        String[] result = RunTestUtil.runTest(Test_.class, args);
        Assertions.assertSame(args, result);
        Assertions.assertEquals(1, result.length);
        Assertions.assertEquals("hmmm", result[0]);
    }

    @Test
    void testInterfaceWithDefaultImpl() {
        String[] args = {"hmm"};
        RunTestUtil.runTest(INumberVisitor.class, args);
        Assertions.assertEquals("done", args[0]);
    }

    static class Test_ {
        @DispatchHub
        public void dis() {

        }

        public static void main(String[] args) {
            args[0] = "hmmm";
        }
    }
}
