package asmext.tree.analysis.dataflow.util.analyzer;

import asmext.tree.analysis.dataflow.DataFlowAnalyzer;
import asmext.tree.analysis.dataflow.DataFlowFrame;
import asmext.tree.analysis.dataflow.util.analyzer.bytecode.BytecodeProvider;
import asmext.tree.analysis.dataflow.value.DataFlowValue;
import asmlib.util.ByteCodeClassLoader;
import org.objectweb.asm.tree.analysis.Frame;

import java.lang.reflect.InvocationTargetException;

public abstract class AbstractHistoryAnalyzerTest {


    protected static final RuntimeException RETURN_EXCEPTION = new RuntimeException();
    static int[] exitValue;

    @SuppressWarnings("unused")
    public static void exit(int x) {
        exitValue = new int[]{x};
        throw RETURN_EXCEPTION;
    }

    public static DataFlowAnalyzer makeAnalyzer(Runnable mergeActor, long[] mergeTime) {
        return new DataFlowAnalyzer(true) {
            @Override
            protected DataFlowFrame newFrame(int numLocals, int numStack) {
                mergeTime[0] = System.nanoTime();
                return new MyDataFlowFrame(numLocals, numStack, mergeActor, this);
            }

            @Override
            protected DataFlowFrame newFrame(Frame<? extends DataFlowValue> frame) {
                return new MyDataFlowFrame(frame, mergeActor, this);
            }
        };
    }

    protected static BytecodeProvider.CommonTestThing testRunnable(byte[] bytecode) throws InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        ByteCodeClassLoader loader = new ByteCodeClassLoader(bytecode);

        return (BytecodeProvider.CommonTestThing) loader.loadClass(BytecodeProvider.TARGET_CLASS_NAME).getConstructors()[0].newInstance();
    }

}