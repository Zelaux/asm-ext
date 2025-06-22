package asmlib.analytics;

import asmlib.util.NodeUtil;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.*;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ObjectCreatingIndexerTest {

    public static final int API = Opcodes.ASM9;

    private static String string(List<ObjectCreationRange> rootRanges) {
        return rootRanges
                .stream()
                .map(ObjectCreationRange::toString)
                .collect(Collectors.joining("\n"))
                ;
    }

    @lombok.SneakyThrows
    @Test
    public void testCorrect() {
        val classNode = NodeUtil.classNode(ObjectCreatingIndexerTest.class, API);
        val methodNode = NodeUtil.extractMethod(classNode, "methodToTest", "()V");
        val visitor = ObjectCreationRangeAnalyzer.visitor(API);
        methodNode.accept(visitor);

        assertEquals(2, visitor.rootRanges.size());
        assertEquals(1, visitor.rootRanges.get(1).innerObjects.size());

    }

    @lombok.SneakyThrows
    @Test
    public void testEqual() {

        val methodNode = NodeUtil.methodNode(ObjectCreatingIndexerTest.class.getDeclaredMethods()[0], API);
        val visitor = ObjectCreationRangeAnalyzer.visitor(API);
        methodNode.accept(visitor);
        val expectedResult = visitor.toResult();
        val actualResult = ObjectCreationRangeAnalyzer.analyze(API, methodNode);
        assertEquals( string(expectedResult.rootRanges), string(actualResult.rootRanges),"rootRanges");
        assertEquals( string(expectedResult.allRanges), string(actualResult.allRanges),"allRanges");

    }

    @SuppressWarnings("unused")
    void methodToTest() throws URISyntaxException, FileNotFoundException {
        byte[] bytes = new byte[0];
        String string = new String(bytes, 0, bytes.length);
        int i = 0;
        int i1 = 0;
        int i2 = 0;
        //noinspection resource
        FileOutputStream stream = new FileOutputStream(new File(new URI("")), true);


    }
}
