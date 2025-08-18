package asmlib.util;

import asmlib.util.testclasses.Bar;
import asmlib.util.testclasses.Baz;
import asmlib.util.testclasses.Buux;
import asmlib.util.testclasses.Foo;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClassFileMetaDataLombokTest {
    private static ClassFileMetaDataLombok foo = create(Foo.class);
    private static ClassFileMetaDataLombok bar = create(Bar.class);
    private static ClassFileMetaDataLombok baz = create(Baz.class);
    private static ClassFileMetaDataLombok buux = create(Buux.class);


    static ClassFileMetaDataLombok create(Class<?> clazz) {
        return new ClassFileMetaDataLombok(classBytes(clazz));
    }

    @SneakyThrows
    static byte @NotNull [] classBytes(Class<?> clazz) {
        InputStream resource = ClassLoader.getSystemResourceAsStream(clazz.getName().replace('.', '/')+".class");
        assert resource != null;
        byte[] bytes = resource.readAllBytes();
        resource.close();
        return bytes;
    }

    @Test
    public void testGetClassName() {
        assertTrue(foo.containsUtf8("Foo"));
        assertEquals("Foo", foo.getClassName());

        assertTrue(bar.containsUtf8("Bar"));
        assertEquals("Bar", bar.getClassName());

        assertTrue(baz.containsUtf8("Baz"));
        assertEquals("Baz", baz.getClassName());
    }

    @Test
    public void testGetSuperClassName() {
        assertTrue(foo.containsUtf8("java/lang/Object"));
        assertEquals("java/lang/Object", foo.getSuperClassName());

        assertEquals("java/lang/Object", bar.getSuperClassName());
        assertEquals("java/lang/Object", baz.getSuperClassName());

        assertEquals("java/util/ArrayList", buux.getSuperClassName());
    }

    @Test
    public void testUsesClass() {
        assertTrue(foo.usesClass("java/lang/System"));
//		assertTrue(foo.usesClass("java/lang/String"));
    }

    @Test
    public void testUsesField() {
        assertTrue(foo.usesField("java/lang/System", "out"));
    }

    @Test
    public void testUsesMethodWithName() {
        assertTrue(foo.usesMethod("java/io/PrintStream", "print"));

        assertTrue(buux.usesMethod("java/util/ArrayList", "<init>"));
        assertTrue(buux.usesMethod("java/util/ArrayList", "add"));
        assertTrue(buux.usesMethod("Buux", "addSomething"));
    }

    @Test
    public void testUsesMethodWithNameAndDescriptor() {
        assertTrue(foo.usesMethod("java/io/PrintStream", "print", "(Ljava/lang/String;)V"));

        assertTrue(buux.usesMethod("java/util/ArrayList", "<init>", "(I)V"));
        assertTrue(buux.usesMethod("java/util/ArrayList", "add", "(Ljava/lang/Object;)Z"));
        assertTrue(buux.usesMethod("Buux", "addSomething", "()V"));
    }

    @Test
    public void testGetInterfaces() {
        assertTrue(foo.containsUtf8("java/util/RandomAccess"));

        List<String> fooInterfaces = foo.getInterfaces();
        assertEquals(1, fooInterfaces.size());
        assertEquals("java/util/RandomAccess", fooInterfaces.get(0));

        assertTrue(bar.containsUtf8("java/util/RandomAccess"));
        assertTrue(bar.containsUtf8("java/util/Map"));

        List<String> barInterfaces = bar.getInterfaces();
        assertEquals(2, barInterfaces.size());
        assertEquals("java/util/RandomAccess", barInterfaces.get(0));
        assertEquals("java/util/Map", barInterfaces.get(1));
    }

    @Test
    public void testContainsStringConstant() {
        assertTrue(foo.containsStringConstant("Eén"));
        assertTrue(foo.containsStringConstant("TwoFour"));

        assertTrue(buux.containsStringConstant("H\u3404l\0"));

        assertFalse(foo.containsStringConstant("Seven"));
    }

    @Test
    public void testContainsDouble() {
        assertTrue(foo.containsDouble(1.23));
        assertTrue(foo.containsDouble(Double.NaN));
        assertTrue(foo.containsDouble(Double.POSITIVE_INFINITY));
        assertTrue(foo.containsDouble(Double.NEGATIVE_INFINITY));

        assertFalse(foo.containsDouble(1.0));
        assertFalse(buux.containsDouble(1.0));
        assertFalse(buux.containsDouble(Double.NaN));
        assertFalse(buux.containsDouble(Double.POSITIVE_INFINITY));
        assertFalse(buux.containsDouble(Double.NEGATIVE_INFINITY));
    }

    @Test
    public void testContainsFloat() {
        assertTrue(foo.containsFloat(1.23F));
        assertTrue(foo.containsFloat(Float.NaN));
        assertTrue(foo.containsFloat(Float.POSITIVE_INFINITY));
        assertTrue(foo.containsFloat(Float.NEGATIVE_INFINITY));

        assertFalse(foo.containsFloat(1.0F));
        assertFalse(buux.containsFloat(1.0F));
        assertFalse(buux.containsFloat(Float.NaN));
        assertFalse(buux.containsFloat(Float.POSITIVE_INFINITY));
        assertFalse(buux.containsFloat(Float.NEGATIVE_INFINITY));
    }

    @Test
    public void testContainsInteger() {
        assertTrue(foo.containsInteger(123));

        assertFalse(foo.containsInteger(1));
        assertFalse(buux.containsInteger(1));
    }

    @Test
    public void testContainsLong() {
        assertTrue(foo.containsLong(123));
        assertTrue(foo.containsLong(0x1FFFFFFFFL));

        assertFalse(foo.containsLong(1));
        assertFalse(buux.containsLong(1));
    }

}