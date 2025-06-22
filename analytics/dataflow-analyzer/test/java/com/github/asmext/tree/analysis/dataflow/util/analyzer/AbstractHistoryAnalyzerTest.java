package com.github.asmext.tree.analysis.dataflow.util.analyzer;

import asmlib.util.ByteCodeClassLoader;
import com.github.asmext.tree.analysis.dataflow.util.analyzer.bytecode.BytecodeProvider;
import com.github.asmext.tree.analysis.dataflow.DataFlowAnalyzer;
import com.github.asmext.tree.analysis.dataflow.value.DataFlowValue;

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
        return new DataFlowAnalyzer() {
            @Override
            protected Frame<DataFlowValue> newFrame(int numLocals, int numStack) {
                mergeTime[0]=System.nanoTime();
                return new MyDataFlowFrame(numLocals, numStack, mergeActor);
            }

            @Override
            protected Frame<DataFlowValue> newFrame(Frame<? extends DataFlowValue> frame) {
                return new MyDataFlowFrame(frame, mergeActor);
            }
        };
    }

    protected static BytecodeProvider.CommonTestThing testRunnable(byte[] bytecode) throws InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        ByteCodeClassLoader loader = new ByteCodeClassLoader(bytecode);

        return (BytecodeProvider.CommonTestThing) loader.loadClass(BytecodeProvider.TARGET_CLASS_NAME).getConstructors()[0].newInstance();
    }

}