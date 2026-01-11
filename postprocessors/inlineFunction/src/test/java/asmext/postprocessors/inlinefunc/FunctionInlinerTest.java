package asmext.postprocessors.inlinefunc;

import asmext.postprocessors.inlinefunc.classes.BooleanTestClass;
import asmext.postprocessors.inlinefunc.classes.ModificationTestClass;
import asmext.postprocessors.inlinefunc.classes.SimpleTestClass;

import asmlib.util.NodeUtil;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;

class FunctionInlinerTest {
    static File mainFolder;

    @SneakyThrows
    public static void main(String[] args) {
        mainFolder = new File("gitignore/inline-func");

        Class<?>[] classes = {
                SimpleTestClass.class,
                BooleanTestClass.class,
                ModificationTestClass.class,
        };
        for (var it : classes) testClass(it);
    }

    @SneakyThrows
    static void testClass(Class<?> testClass) {

        ClassNode classNode = NodeUtil.classNode(testClass, Opcodes.ASM9);


        Type ownerType = Type.getType(testClass);
        var inlineMethods = new HashMap<String, MethodToInline>();
        var methods = new ArrayList<MethodNode>();

        for (var method : classNode.methods) {
            if (NodeUtil.findAnnotation(method.invisibleAnnotations, TestInline.class) != null) {
                inlineMethods.put(descriptor(ownerType, method), MethodToInline.make(method));
            } else if (NodeUtil.findAnnotation(method.invisibleAnnotations, TestTarget.class) != null) {
                methods.add(method);
            }
        }

        for (var method : methods) {
            method.instructions = FunctionInliner.inline(FunctionInliner.makeDescriptor(ownerType, method), inlineMethods);
        }
//        dot.instructions = FunctionInliner.inline(new FunctionInliner.MethodDescriptor(ownerType, dot), dot.instructions, inlineMethods);
//        dot_i.instructions = FunctionInliner.inline(FunctionInliner.makeDescriptor(ownerType, dot_i), inlineMethods);


        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        classNode.accept(writer);

        File file = new File(mainFolder, testClass.getSimpleName() + ".class");
        Files.createDirectories(file.getParentFile().toPath());
        Files.write(
                file.toPath(),
                writer.toByteArray()
        );
    }

    private static String descriptor(Type owner, MethodNode node) {
        return owner.getInternalName() + "#" + node.name + node.desc;
    }

    @Test
    void name() {

    }

}