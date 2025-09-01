package asmext.analytics.controlflow;

import asmext.analytics.controlflow.bytecode.*;
import asmext.analytics.controlflow.methodgen.CommonTestThing;
import asmext.analytics.controlflow.methodgen.MethodGenerator;
import asmext.analytics.controlflow.methodgen.TestClassGenerator;
import asmlib.util.ByteCodeClassLoader;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * @author Zelaux
 * @date 01.09.2025 1:27
 */
class ControlFlowAnalyzerTest {
    public static final Type TYPE = Type.getType(ControlFlowAnalyzerTest.class);
    public static final ByteCodeClassLoader classLoader = new ByteCodeClassLoader();

    static Stream<Arguments> generatorsWithCases() {
        return generators()
                .filter(MethodGenerator::hasCases)
                .flatMap(x -> {
                    CommonTestThing obj = createObject(x);
                    return Arrays.stream(x.cases()).map(testCase -> Arguments.of(obj, testCase));
                });

    }

    static Stream<Arguments> bytecodes() {
        return generators()
                .filter(MethodGenerator::hasCases)
                .map(x -> Arguments.of(x.getClass(), getBytes(x)));
    }

    static Stream<Arguments> methodNodes() {
        return generators()
                .filter(MethodGenerator::hasCases)
                .map(x -> Arguments.of(x, x.asNode(TYPE)));
    }

    @SneakyThrows
    private static @NotNull CommonTestThing createObject(MethodGenerator x) {
        var classLoader = new ByteCodeClassLoader();
        classLoader.addByteCode(getBytes(x));
        return (CommonTestThing) classLoader.loadClass(TestClassGenerator.TARGET_CLASS_NAME).getConstructors()[0].newInstance();
    }

    @SneakyThrows
    private static byte[] getBytes(MethodGenerator x) {
        return x.getBytes(TYPE);
    }

    static Stream<MethodGenerator> generators() {
        return Stream.of(new GotoEnd(), new SingleBranchIfStmt(),new DoubleBranchIfStmt(),new DoWhileStmt(),new WhileStmt());
    }

    private static String @NotNull [] listToString(ArrayList<ControlFlowBlock> blocks) {
        String[] array = new String[blocks.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = blocks.get(i).debugString();
        }
        return array;
    }

    @SneakyThrows
    @ParameterizedTest()
    @MethodSource("methodNodes")
    void name(MethodGenerator generator, MethodNode node) {
        ControlFlowAnalyzer analyzer = new ControlFlowAnalyzer();
        ControlFlowNode[] analyzed = analyzer.analyze(TestClassGenerator.TARGET_CLASS_NAME, node);
        Assertions.assertEquals(node.instructions.size(), analyzed.length);
        ArrayList<ControlFlowBlock> blocks = analyzer.collectGroups();

        Assertions.assertEquals(
                String.join("\n", generator.controlFlowBlocks()),
                String.join("\n", listToString(blocks))
        );


    }

    @ParameterizedTest()
    @MethodSource("generatorsWithCases")
    void testCases(CommonTestThing thing, TestCase testCase) {
        Assertions.assertEquals(testCase.result(), thing.test(testCase.a(), testCase.b(), testCase.c(), testCase.args()));
    }

    @SneakyThrows
    @ParameterizedTest()
    @MethodSource("bytecodes")
    void saveBytecode(Class<?> type, byte[] byteCode) {
        String className = new ClassReader(byteCode).getClassName();
        TestAsmUtil.debugSaveByteCode(type.getName(), byteCode, className);
    }

    @SneakyThrows
    @ParameterizedTest()
    @MethodSource("bytecodes")
    void testReadable(Class<?> type, byte[] byteCode) {
        ClassReader reader = new ClassReader(byteCode);
        reader.accept(new TraceClassVisitor(new PrintWriter(new DummyWriter())), 0);
    }

    private static class DummyWriter extends Writer {
        @Override
        public void write(@NotNull char[] cbuf, int off, int len) throws IOException {

        }

        @Override
        public void flush() throws IOException {

        }

        @Override
        public void close() throws IOException {

        }
    }
}