package asmlib.util;

import lombok.*;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.lang.reflect.*;

import static asmlib.util.MethodStuff.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MethodSaveTest{
    @SuppressWarnings("SameParameterValue")
    private static void assertOutput(String expected, ThrowingRunnable runnable) throws Throwable{
        PrintStream prevOut = System.out;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        runnable.run();
        System.setOut(prevOut);
        assertEquals(expected, out.toString().replace(System.lineSeparator(),"\n"));
    }

    @SneakyThrows
    @Test
    public void test(){
        byte[] bytes1 = saveMethodBytecode(MyClass.class, "anotherMethod", "(II)V");
        byte[] bytes2 = saveMethodBytecode(MyClass.class, "anotherMethod2", "(II)V");
        byte[] bytes3 = saveMethodBytecode(MyClass.class, "anotherMethod3", "(II)I");

        byte[] generatedBytes = mergeSavedMethods("TestClass", "testMethod", "(II)I", bytes1, bytes1, bytes1, bytes2, bytes3);
        ByteCodeClassLoader loader = new ByteCodeClassLoader(generatedBytes);
        Class<?> testClass = loader.loadClass("TestClass");
        Method method = testClass.getDeclaredMethod("testMethod", int.class, int.class);
        method.setAccessible(true);


        assertOutput(
            "a: 3\na: 3\na: 3\nb: 4\n",
            () -> assertEquals(7, (int)method.invoke(null, 3, 4))
        );
    }

    @SuppressWarnings("unused")
    public static class MyClass{
        // Другой метод для демонстрации выборочного сохранения
        public static void anotherMethod(int a, int b){
            System.out.println("a: " + a);
        }

        public static void anotherMethod2(int a, int b){
            System.out.println("b: " + b);
        }

        public static int anotherMethod3(int a, int b){
            return a + b;
        }

        public void exampleMethod(){
            System.out.println("Hello from exampleMethod!");
        }
    }

}
