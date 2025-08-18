package asmlib.util;

import asmlib.util.testclasses.Test2;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.StringJoiner;
import java.util.stream.Collectors;

import static asmlib.util.ClassFileMetaDataLombokTest.classBytes;
import static org.junit.jupiter.api.Assertions.*;

class ClassFileMetaDataTest {
    static ClassFileMetaData meta = new ClassFileMetaData(classBytes(Test2.class));

    @Test
    void usedMethods() {
        String methods = meta.usedMethods()
                .map(ClassFileMetaData.ClassMethod::toString)
                .sorted()
                .collect(Collectors.joining("\n"));
        assertEquals("""
                asmlib/util/testclasses/Test2#number()I
                java/io/PrintStream#println(I)V
                java/io/PrintStream#println(Ljava/lang/Object;)V
                java/io/PrintStream#println(Ljava/lang/String;)V
                java/lang/Class#getInterfaces()[Ljava/lang/Class;
                java/lang/Object#<init>()V
                java/util/List#size()I[interface]""", methods);
    }

    @Test
    void usedFields() {
        String fields = meta.usedFields()
                .map(ClassFileMetaData.ClassField::toString)
                .sorted()
                .collect(Collectors.joining("\n"));
        assertEquals("""
                asmlib/util/testclasses/Test2.magicNumber: I
                asmlib/util/testclasses/Test2.someString: Ljava/lang/String;
                java/lang/System.out: Ljava/io/PrintStream;""", fields);
    }

    @Test
    void usedClasses() {
        String classes = meta.usedClasses()
                .sorted()
                .collect(Collectors.joining("\n"));
        assertEquals("""
                asmlib/util/testclasses/Test2
                java/io/PrintStream
                java/lang/Class
                java/lang/Object
                java/lang/System
                java/util/List""", classes);
    }

    @Test
    void usesAnnotation() {
        assertTrue(meta.usesAnnotation(Deprecated.class));
        assertTrue(meta.usesAnnotation("Ljava/lang/Deprecated;"));
        assertFalse(meta.usesAnnotation(FunctionalInterface.class));
        assertFalse(meta.usesAnnotation("Ljava/lang/FunctionalInterface;"));
    }

    @Test
    void typesAsString() {
        assertEquals("""
                null, METHOD, CLASS, NAME_TYPE, UTF8, UTF8, UTF8, STRING, UTF8, FIELD, CLASS
                NAME_TYPE, UTF8, UTF8, UTF8, METHOD, CLASS, NAME_TYPE, UTF8, UTF8, UTF8, FIELD
                CLASS, NAME_TYPE, UTF8, UTF8, UTF8, INTERFACE_METHOD, CLASS, NAME_TYPE, UTF8, UTF8, UTF8
                METHOD, CLASS, NAME_TYPE, UTF8, UTF8, UTF8, METHOD, NAME_TYPE, UTF8, METHOD, NAME_TYPE
                UTF8, FIELD, NAME_TYPE, UTF8, UTF8, METHOD, NAME_TYPE, UTF8, UTF8, UTF8, UTF8
                UTF8, UTF8, UTF8, UTF8, UTF8, UTF8, UTF8, UTF8, UTF8, UTF8, UTF8
                UTF8""", toMatrix(meta.typesAsString(), 11));
    }

    @SuppressWarnings("SameParameterValue")
    @SneakyThrows
    private static @NotNull String toMatrix(String[] elements, int targetWidth) {
        int resultLen = (elements.length + targetWidth - 1) / targetWidth;
        String[] strings = new String[resultLen];

        for (int i = 0; i < resultLen; i++) {
            int start = i * targetWidth;
            int end = Math.min(elements.length, start + targetWidth);
            StringJoiner joiner = new StringJoiner(", ");
            for (int j = start; j < end; j++) {
                joiner.add(elements[j]);
            }
            strings[i] = joiner.toString();
        }
        return String.join("\n", strings);
    }
}