package asmext.tree.analysis.dataflow.util.analyzer;

import asmext.tree.analysis.dataflow.TestAsmUtil;
import asmext.tree.analysis.dataflow.util.analyzer.bytecode.BytecodeProvider;
import asmext.tree.analysis.dataflow.util.analyzer.bytecode.IfAndLoopBytecode;
import asmext.tree.analysis.dataflow.util.analyzer.bytecode.IfBytecode;
import asmext.tree.analysis.dataflow.util.analyzer.bytecode.InifiniteLoopDumpClass;
import asmext.tree.analysis.dataflow.DataFlowAnalyzer;
import asmext.tree.analysis.dataflow.meta.RecursiveMeta;
import asmext.tree.analysis.dataflow.value.CommonDataFlowValue;
import asmext.tree.analysis.dataflow.value.DataFlowValue;
import asmext.tree.analysis.dataflow.value.MergedDataFlowValue;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class AnalyzerTest extends AbstractHistoryAnalyzerTest {

    public static final Type TYPE = Type.getType(AnalyzerTest.class);

    @SneakyThrows
    public static Stream<Arguments> bytecodes() {
        return providers().map(provider -> provider.toJunitArgument(TYPE));
    }

    private static Stream<Arguments> providersArg() {
        return providers().map(Arguments::of);
    }

    private static Stream<BytecodeProvider> providers() {
        return Stream.of(
            new IfBytecode(),
            new InifiniteLoopDumpClass(),
            new IfAndLoopBytecode()
        );
    }

    @Test
    void infiniteLoop() throws Exception {
        byte[] bytecode = new InifiniteLoopDumpClass().generate(TYPE);
        TestAsmUtil.debugSaveByteCode("infiniteLoop", bytecode, BytecodeProvider.TARGET_CLASS_NAME);
        BytecodeProvider.CommonTestThing object = testRunnable(bytecode);

        for(int i = 0; i < 10; i++) {
            try {
                object.test(i,-1,-1);
                Assertions.fail("Expected return exception");
            } catch(Exception e) {
                if(e != RETURN_EXCEPTION) {throw e;
                }
            }
            assertArrayEquals(new int[]{i + 1}, exitValue);
        }

        ClassNode classNode = TestAsmUtil.getClassNode(bytecode);
        MethodNode methodNode = classNode.methods.get(1);
        Frame<DataFlowValue>[] frames = computeFramesWithMergeCheck(classNode);
        System.out.println(Arrays.toString(frames));

        Frame<DataFlowValue> fifthFrame = frames[5];
        assertEquals(
            "param(LTest;;0){I<-- IINC 1 1 + 1}param(I;2)param(I;3)param([Ljava/lang/Object;;4) {I<-- ILOAD 1 + 1}",
            fifthFrame.toString()
        );

        var loaded=assertInstanceOf(CommonDataFlowValue.class,fifthFrame.getStack(0));
        assertEquals(1, loaded.previousValues.length);

        var iinc = assertInstanceOf(CommonDataFlowValue.class, loaded.previousValues[0]);
        assertEquals(Opcodes.IINC,iinc.producer.getOpcode());
        assertEquals(1, iinc.previousValues.length);

        var reqMerged = assertInstanceOf(MergedDataFlowValue.class, iinc.previousValues[0]);
        assertEquals(1,reqMerged.getMetaEntries().count());

        var handled = reqMerged.getMeta(RecursiveMeta.HandledRecursiveMeta.meta);
        assertNotNull(handled);
        handled.owner();

    }

    @Test
    void testIf() throws Exception {
        byte[] bytecode = new IfBytecode().generate(TYPE);
        TestAsmUtil.debugSaveByteCode("testIf", bytecode, BytecodeProvider.TARGET_CLASS_NAME);
        BytecodeProvider.CommonTestThing object = testRunnable(bytecode);

        assertEquals(-1,object.test(0, 1, -1));
        assertEquals(1,object.test(1, 1, -1));

        Frame<DataFlowValue>[] frames = getFrames(() -> {}, new long[1], TestAsmUtil.getClassNode(bytecode));
        assertEquals(
            "<|[{I<-- ILOAD 3 + 1}, {I<-- ILOAD 2 + 1}]|>",
            frames[9].getStack(0).toString()
        );
    }

    @ParameterizedTest
    @MethodSource("bytecodes")
    @SneakyThrows
    void testByteCode(Class<? extends BytecodeProvider> providerClass, byte[] bytecode) {

        testRunnable(bytecode);
        ClassNode classNode = TestAsmUtil.getClassNode(bytecode);
        var frames = computeFramesWithMergeCheck(classNode);

    }

    private static Frame<DataFlowValue>[] computeFramesWithMergeCheck(ClassNode classNode) throws AnalyzerException {
        int[] mergeCounter = {0};
        long[] mergeTime = {System.nanoTime()};
        Runnable mergeActor = () -> {
            long l = System.nanoTime();
            long delta = l - mergeTime[0];
            long deltaMS = TimeUnit.NANOSECONDS.toMillis(delta);
            mergeTime[0] = l;
            if(++mergeCounter[0] > 1000)
                throw new RuntimeException("Suspicious amount of merging");
            if(deltaMS > 5000) throw new RuntimeException("Suspicious time between merge: " + deltaMS + "ms");
        };
        return getFrames(mergeActor, mergeTime, classNode);
    }

    private static Frame<DataFlowValue>[] getFrames(Runnable mergeActor, long[] mergeTime, ClassNode classNode) throws AnalyzerException {
        DataFlowAnalyzer analyzer = makeAnalyzer(mergeActor, mergeTime);
        MethodNode method = classNode.methods.get(1);

        return analyzer.analyze(classNode.name, method);
    }
}
