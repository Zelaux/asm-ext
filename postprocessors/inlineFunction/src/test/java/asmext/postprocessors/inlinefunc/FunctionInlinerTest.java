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
        mainFolder = new File("gitignore/inlineDot-func");


        ClassNode classNode = NodeUtil.classNode(TestClass.class, Opcodes.ASM9);
        Map<String, MethodNode> methods = classNode.methods.stream().collect(
                Collectors.toMap(x -> x.name, x -> x));

        MethodNode x = methods.get("x"), y = methods.get("y"), dot = methods.get("dot");
        MethodNode dotx = methods.get("dotx"), doty = methods.get("doty"), dot_i = methods.get("dot_i");

        Type ownerType = Type.getType(TestClass.class);

        var inlineMethods = new HashMap<>(Map.of(
                descriptor(ownerType, x), MethodToInline.make(x),
                descriptor(ownerType, y), MethodToInline.make(y),
                descriptor(ownerType, dotx), MethodToInline.make(dotx),
                descriptor(ownerType, doty), MethodToInline.make(doty)
        ));
//        dot.instructions = FunctionInliner.inline(new FunctionInliner.MethodDescriptor(ownerType, dot), dot.instructions, inlineMethods);
        dot_i.instructions = FunctionInliner.inline(FunctionInliner.makeDescriptor(ownerType, dot_i), inlineMethods);


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

}