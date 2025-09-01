package asmext.analytics.controlflow.methodgen;

import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.MethodNode;

public class TestClassGenerator implements Opcodes {
    public static final String TARGET_CLASS_NAME = "Test";
    public static final Type SELF_TYPE = getTypeFromClassName(TARGET_CLASS_NAME);
    public static final String TARGET_METHOD_NAME = "test";
    public static final String METHOD_DESCRIPTOR = Type.getMethodDescriptor(
            Type.INT_TYPE,

            Type.INT_TYPE,
            Type.INT_TYPE,
            Type.INT_TYPE,
            Type.getType(Object[].class)
    );
    public static final Method TARGET_METHOD_OBJ = new Method(TARGET_METHOD_NAME, METHOD_DESCRIPTOR);
    public final MethodGenerator generator;

    public TestClassGenerator(MethodGenerator generator) {this.generator = generator;}

    /*TODO UTIL METHOD*/
    protected static Type getTypeFromClassName(String targetClassName) {
        return Type.getType('L' + targetClassName.replace('.', '/') + ';');
    }


    public void initTestMethod(ClassWriter classWriter, Type ownerType) throws Exception {

        MethodVisitor mv = classWriter.visitMethod(ACC_PUBLIC, TARGET_METHOD_NAME, METHOD_DESCRIPTOR, null, null);

        MethodNode visitor = getMethodNode(ownerType);
        visitor.accept(mv);

//        generator.visitMaxs(-1, -1);
//        generator.endMethod();
    }

    @SneakyThrows
    public @NotNull MethodNode getMethodNode(Type ownerType) {
        MethodNode visitor = new MethodNode(ASM9, ACC_PUBLIC, TARGET_METHOD_NAME, METHOD_DESCRIPTOR, null, null);
        generator.setMethodVisitor(visitor);
        visitor.visitMaxs(20, 20);
        generator.generateInternal(SELF_TYPE, ownerType);
        generator.setMethodVisitor(null);
        return visitor;
    }

    public void initConstructor(ClassWriter classWriter, Type superType) {
        MethodVisitor mv;
        mv = classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        Label label0 = new Label();
        mv.visitLabel(label0);
        mv.visitLineNumber(3, label0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitLdcInsn(Type.getType(generator.getClass()));
        mv.visitMethodInsn(INVOKESPECIAL, superType.getInternalName(), "<init>", "(Ljava/lang/Class;)V", false);
        mv.visitInsn(RETURN);
        Label label1 = new Label();
        mv.visitLabel(label1);
        mv.visitMaxs(-1, -1);
        mv.visitEnd();
    }

    public byte[] generate(Type ownerClass) throws Exception {
        Type superType = Type.getType(CommonTestThing.class);
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

        classWriter.visit(V17, ACC_PUBLIC | ACC_SUPER, SELF_TYPE.getInternalName(), null, superType.getInternalName(), null);


        classWriter.visitSource("Test.java", null);

        initConstructor(classWriter, superType);
        initTestMethod(classWriter, ownerClass);
        classWriter.visitEnd();

        return classWriter.toByteArray();
    }

}
