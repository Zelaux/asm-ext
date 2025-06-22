package com.github.asmext.tree.analysis.dataflow.util.analyzer.bytecode;

import lombok.SneakyThrows;
import org.junit.jupiter.params.provider.Arguments;
import org.objectweb.asm.*;

public abstract class BytecodeProvider implements Opcodes {
    public static final String TARGET_CLASS_NAME = "Test";
    public static final String TARGET_METHOD_NAME = "test";

    public abstract void generateInternal(MethodVisitor mv, Type selfType, Type owner) throws Exception;


    public static abstract class CommonTestThing {

        public abstract int test(int a, int b, int c, Object... varArgs);

    }

    @SneakyThrows
    public Arguments toJunitArgument(Type type) {
        return Arguments.of(getClass(), generate(type));
    }


    public byte[] generate(Type ownerClass) throws Exception {
        Type superType = Type.getType(CommonTestThing.class);
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

        Type selfType = getTypeFromClassName(TARGET_CLASS_NAME);
        classWriter.visit(V17, ACC_PUBLIC | ACC_SUPER, selfType.getInternalName(), null, superType.getInternalName(), null);


        classWriter.visitSource("Test.java", null);

        initConstructor(classWriter, superType);
        initTestMethod(classWriter, selfType, ownerClass);
        classWriter.visitEnd();

        return classWriter.toByteArray();
    }
/*TODO UTIL METHOD*/
    protected static Type getTypeFromClassName(String targetClassName) {
        return Type.getType('L' + targetClassName.replace('.', '/') + ';');
    }

    public void initTestMethod(ClassWriter classWriter, Type selfType, Type ownerType) throws Exception {
        var mv = classWriter.visitMethod(ACC_PUBLIC, TARGET_METHOD_NAME,
            Type.getMethodDescriptor(
                Type.INT_TYPE,

                Type.INT_TYPE,
                Type.INT_TYPE,
                Type.INT_TYPE,
                Type.getType(Object[].class)
            )
            , null, null);
        mv.visitCode();
                generateInternal(mv, selfType, ownerType);
        mv.visitMaxs(-1, -1);
        mv.visitEnd();
    }

    public void initConstructor(ClassWriter classWriter, Type superType) {
        MethodVisitor methodVisitor;
        methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        methodVisitor.visitCode();
        Label label0 = new Label();
        methodVisitor.visitLabel(label0);
        methodVisitor.visitLineNumber(3, label0);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, superType.getInternalName(), "<init>", "()V", false);
        methodVisitor.visitInsn(RETURN);
        Label label1 = new Label();
        methodVisitor.visitLabel(label1);
        methodVisitor.visitMaxs(-1, -1);
        methodVisitor.visitEnd();
    }
}
