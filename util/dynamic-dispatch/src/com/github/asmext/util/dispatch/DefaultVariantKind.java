package com.github.asmext.util.dispatch;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import static com.github.asmext.util.dispatch.DispatcherGenerator.loadInsn;

public abstract class DefaultVariantKind implements Opcodes {
    public static final DefaultVariantKind invokeSuper = new DefaultVariantKind() {
        @Override
        public void process(MethodVisitor mv, Type ownerType, Type[] dispatcherParameters, MethodInfo dispatcherMethod) {
            mv.visitVarInsn(ALOAD, 0);
            for (int i = 0; i < dispatcherParameters.length; i++) {
                loadInsn(mv, dispatcherParameters[i], dispatcherMethod.realVarIndex(i));
            }
            mv.visitMethodInsn(
                    INVOKESPECIAL,
                    ownerType.getInternalName(),
                dispatcherMethod.name,
                dispatcherMethod.descriptor,
                    false
            );

            mv.visitInsn(dispatcherMethod.returnType.getOpcode(IRETURN));
        }
    };
    public static final DefaultVariantKind invokeOther = new DefaultVariantKind() {
        @Override
        public void process(MethodVisitor mv, Type ownerType, Type[] dispatcherParameters, MethodInfo dispatcherMethod) {
            mv.visitVarInsn(ALOAD, 0);
            for (int i = 0; i < dispatcherParameters.length; i++) {
                loadInsn(mv, dispatcherParameters[i], dispatcherMethod.realVarIndex(i));
            }
            mv.visitMethodInsn(
                INVOKEVIRTUAL,
                ownerType.getInternalName(),
                dispatcherMethod.bytecode.name,
                dispatcherMethod.descriptor,
                false
            );

            mv.visitInsn(dispatcherMethod.returnType.getOpcode(IRETURN));
        }
    };
    public static final DefaultVariantKind inlineBytecode = new DefaultVariantKind() {
        @Override
        public void process(MethodVisitor mv, Type ownerType, Type[] dispatcherParameters, MethodInfo dispatcherMethod) {
            dispatcherMethod.bytecode.instructions.accept(mv);
        }
    };

    public abstract void process(MethodVisitor mv, Type ownerType, Type[] dispatcherParameters, MethodInfo dispatcherMethod);
}
