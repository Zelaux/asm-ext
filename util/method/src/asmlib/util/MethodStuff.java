package asmlib.util;

import lombok.*;
import org.jetbrains.annotations.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public class MethodStuff {
    // Сохранение байткода метода в файл
    @SneakyThrows
    public static byte[] saveMethodBytecode(Class<?> clazz, String methodName, String methodDesc) {
        final int api = Opcodes.ASM9;
        ClassNode rootClass = new ClassNode(api);
        {
            ClassReader reader = new ClassReader(clazz.getName());
            reader.accept(rootClass, 0);
        }
        MethodNode rootMethodNode = rootClass.methods
                .stream()
                .filter(it -> it.name.equals(methodName) && it.desc.equals(methodDesc))
                .findFirst()
                .orElseThrow(() -> new NoSuchMethodException(methodName + methodDesc));

        ClassNode generatedClass = new ClassNode(api);
        rootClass.accept(generatedClass);
        generatedClass.visit(generatedClass.version, generatedClass.access,
                "SavedClass",
                null,
                "java/lang/Object",
                null);
        generatedClass.methods.clear();
        generatedClass.methods.add(rootMethodNode);
        {
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES); // Важно для корректной работы
            generatedClass.accept(cw);
            return cw.toByteArray();
        }
    }


    public static byte[] mergeSavedMethods(String testClass, String testMethod, String descriptor, byte[]... loadedBytecodes) {

        MethodNode[] methods = new MethodNode[loadedBytecodes.length];
        for (int i = 0; i < loadedBytecodes.length; i++) {
            byte[] bytes = loadedBytecodes[i];
            ClassNode classNode = new ClassNode();
            new ClassReader(bytes).accept(classNode, 0);
            methods[i] = classNode.methods.get(0);
        }

        MethodNode generatedMethod = mergerMethods(testMethod, descriptor, methods);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        writer.visit(Opcodes.V17, Opcodes.ACC_SUPER, testClass.replace('.', '/'), null, "java/lang/Object", null);
        writer.visitSource("Generated code", null);
        generatedMethod.accept(writer);
        return writer.toByteArray();
    }

    @NotNull
    public static MethodNode mergerMethods(String methodName, String descriptor, MethodNode... methods) {
        MethodNode generatedMethod = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, methodName, descriptor, null, null);
        for (int i = 0; i < methods.length; i++) {
            MethodNode method = methods[i];
            InsnList instructions = method.instructions;
            int total = instructions.size();
            if (total == 0) continue;
            if (i + 1 < methods.length) {
                AbstractInsnNode last = instructions.get(total - 1);
                switch (last.getOpcode()) {
                    case Opcodes.RETURN:
                    case Opcodes.IRETURN:
                    case Opcodes.LRETURN:
                    case Opcodes.DRETURN:
                    case Opcodes.FRETURN:
                    case Opcodes.ARETURN:
                        total--;
                }
            }
            for (int j = 0; j < total; j++) {
                AbstractInsnNode node = instructions.get(j);
                if (i + 1 < methods.length) {
                    int opcode = node.getOpcode();

                    switch (opcode) {
                        case Opcodes.RETURN:
                        case Opcodes.IRETURN:
                        case Opcodes.LRETURN:
                        case Opcodes.DRETURN:
                        case Opcodes.FRETURN:
                        case Opcodes.ARETURN:
                            continue;
                    }
                }
                generatedMethod.instructions.add(node);
            }
            generatedMethod.localVariables.addAll(method.localVariables);
            if (i == 0) {
                generatedMethod.parameters = method.parameters;
            }
            generatedMethod.maxLocals = Math.max(generatedMethod.maxLocals, method.maxLocals);
            generatedMethod.maxStack = Math.max(generatedMethod.maxStack, method.maxStack);
        }
        return generatedMethod;
    }
}
