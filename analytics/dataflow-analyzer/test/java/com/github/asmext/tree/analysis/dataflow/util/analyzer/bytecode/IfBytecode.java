package com.github.asmext.tree.analysis.dataflow.util.analyzer.bytecode;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class IfBytecode extends BytecodeProvider implements Opcodes {

    @Override
    public void generateInternal(MethodVisitor mv, Type selfType, Type owner) throws Exception {

        Label __END_IF = new Label();
        Label __BRANCH_ZERO = new Label();
        Label __BRANCH_NOT_ZERO = new Label();

        mv.visitVarInsn(ILOAD, 1);
        mv.visitJumpInsn(IFEQ, __BRANCH_ZERO);

        mv.visitLabel(__BRANCH_NOT_ZERO);
        mv.visitVarInsn(ILOAD, 2);
        mv.visitJumpInsn(GOTO, __END_IF);

        mv.visitLabel(__BRANCH_ZERO);
        mv.visitVarInsn(ILOAD, 3);
        //mv.visitJumpInsn(GOTO,__END_IF);

        mv.visitLabel(__END_IF);
        mv.visitInsn(IRETURN);
    }


}

