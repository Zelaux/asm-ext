package asmext.postprocessors.inlinefunc;

import asmlib.util.NodeUtil;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

class FunctionInlinerTest {
    static File mainFolder;
    @SneakyThrows
    public static void main(String[] args) {
        mainFolder=new File("gitignore/inline-func");


        ClassNode classNode = NodeUtil.classNode(TestClass.class, Opcodes.ASM9);
        Map<String, MethodNode> methods = classNode.methods.stream().collect(
                Collectors.toMap(x -> x.name, x -> x));

        MethodNode x = methods.get("x"), y = methods.get("y"), dot = methods.get("dot");

        Type ownerType = Type.getType(TestClass.class);
        var methodDescriptor = new FunctionInliner.MethodDescriptor(ownerType, dot);

        InsnList inline = FunctionInliner.inline(
                methodDescriptor, dot.instructions,
                new HashMap<>(Map.of(
                        descriptor(ownerType, x), MethodToInline.make(x),
                        descriptor(ownerType, y), MethodToInline.make(y)
                ))
        );

        Textifier printer = new Textifier();
        inline.accept(new TraceMethodVisitor(printer));
        StringWriter stringWriter = new StringWriter();
        printer.print(new PrintWriter(stringWriter));
        System.out.println(stringWriter);

        dot.instructions=inline;


        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        classNode.accept(writer);

        File file = new File(mainFolder, TestClass.class.getSimpleName() + ".class");
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

    static class TestClass {
        static float[] arr = new float[1 << 8];

        static float x(int i) {return arr[i << 1];}

        static float y(int i) {return arr[(i << 1) + 1];}

        static float dot(int i, float x, float y) {
            return x(i) * x + y(i) * y;
        }
    }
}