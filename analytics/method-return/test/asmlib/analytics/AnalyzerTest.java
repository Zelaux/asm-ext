package asmlib.analytics;

import asmlib.util.NodeUtil;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.RecordDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import lombok.SneakyThrows;
import org.intellij.lang.annotations.MagicConstant;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AnalyzerTest {

    public static final int API = Opcodes.ASM9;
    public static final Class<AnalyzerTest> SELF_CLASS = AnalyzerTest.class;

    @SuppressWarnings("unused")
    static Object getTenInType(@MagicConstant(stringValues = {"Int", "Long", "Byte", "Short"}) String type) {
        if (type.equals("Int")) return 10;
//        if (type.equals("Long")) return 10L;
        Object toReturn = -1.0;
        if (type.equals("Byte")) toReturn = (byte) 10;
        if (type.equals("Short")) //noinspection removal,UnnecessaryBoxing,CachedNumberConstructorCall
            toReturn = new Short((short) 10);
        return toReturn;
    }
    @SuppressWarnings("unused")
    static Node someNode(int magicNumber) {
        if (magicNumber % 10 == 0) return new EnumDeclaration();
        if (magicNumber == 1) return new ClassOrInterfaceDeclaration();
        return new RecordDeclaration();
    }

    private static MethodNode methodNode(String methodName) throws NoSuchMethodException {
        for (Method method : SELF_CLASS.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) return NodeUtil.methodNode(method, API);
        }
        throw new NoSuchMethodException(methodName);
    }

    @SneakyThrows
    @Test
    public void rawResolve() {
        MethodNode methodNode = methodNode("getTenInType");

        TypeAndSubtypes result = ResolveRealMethodReturnType
                .resolveDescriptor(API, null, methodNode);
        assertEquals("Ljava/lang/Object;", result.typeDescriptor);
        assertEquals(
                "Ljava/lang/Byte;Ljava/lang/Double;Ljava/lang/Integer;Ljava/lang/Short;",
                String.join("", result.subtypesDescriptors)
        );
    }

    @SneakyThrows
    @Test
    public void clearResolve() {
        assertEquals(Number.class,
                ResolveRealMethodReturnType.resolveClass(API, null, SELF_CLASS,
                        methodNode("getTenInType")
                ));
        assertEquals(TypeDeclaration.class,
                ResolveRealMethodReturnType.resolveClass(API, null, null,
                        methodNode("someNode")
                ));
    }
}
