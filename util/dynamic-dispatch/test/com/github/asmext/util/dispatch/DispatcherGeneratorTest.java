package com.github.asmext.util.dispatch;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.BasicVerifier;

import java.io.IOException;
import java.util.stream.Stream;

class DispatcherGeneratorTest {

    @Test
    void generateDispatcherBytecode() throws IOException, AnalyzerException {
        Class<?> clazz = DispatchClass.class;
        byte[] bytecode = DispatcherGenerator.generateDispatcherBytecode(clazz);
        String newClassName = clazz.getName() + DispatcherGenerator.generatedPostfix;
        int i = newClassName.lastIndexOf('.');
        if (i != -1) {
            newClassName = newClassName.substring(i);
        }
//        ClassNode classNode = TestAsmUtil.debugSaveByteCode("Dispatcher", bytecode, newClassName);
        ClassNode classNode = new ClassNode();
        new ClassReader(bytecode).accept(classNode, 0);
        Analyzer<BasicValue> analyzer = new Analyzer<>(new BasicVerifier());

        analyzer.analyze(classNode.name, classNode.methods.get(1));
    }

    @ParameterizedTest
    @ArgumentsSource(TestProv.class)
    void correctDispatch(Object[] testCase) {
        Assertions.assertEquals(testCase[0], DispatchClass.instance.haha(testCase[1]));
    }

    public static class DispatchClass {
        static final DispatchClass instance = DispatcherGenerator.generateDispatcher(DispatchClass.class);

        @DispatchHub
        public Object haha(Object test) {
            return test.toString();
        }

        public Float haha(Float flt) {
            return flt * 2;
        }

        public Integer haha(Integer flt) {
            return flt * 3;
        }

        public Long haha(Long flt) {
            return flt * 4;
        }
    }

    public static class TestProv implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
            Object[][] arr = {
                    {"123", "123"},
                    {4f, 2f},
                    {3, 1},
                    {8L, 2L}
            };
            return Stream.of(Arguments.of((Object[]) arr));
        }
    }
}