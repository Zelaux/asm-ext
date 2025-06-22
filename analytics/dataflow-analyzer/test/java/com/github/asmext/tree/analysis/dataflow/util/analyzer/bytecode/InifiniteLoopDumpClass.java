package com.github.asmext.tree.analysis.dataflow.util.analyzer.bytecode;

import org.objectweb.asm.*;

public class InifiniteLoopDumpClass extends BytecodeProvider implements Opcodes {


    @Override
    public void generateInternal(MethodVisitor mv, Type selfType, Type owner) throws Exception {
        var label0 = new Label();
        mv.visitLabel(label0);
        mv.visitLineNumber(6, label0);
        mv.visitIincInsn(1,1);
        mv.visitVarInsn(ILOAD,1);
        mv.visitMethodInsn(INVOKESTATIC, owner.getInternalName(), "exit", "(I)V", false);
        mv.visitJumpInsn(GOTO, label0);
        mv.visitVarInsn(ILOAD,1);
        mv.visitInsn(IRETURN);
        var label1 = new Label();
        mv.visitLabel(label1);
        mv.visitLocalVariable("i", "I", null, label0, label1, 0);
    }


}

