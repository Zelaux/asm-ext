package com.github.asmext.tree.analysis.dataflow.util.analyzer.bytecode;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

public class IfAndLoopBytecode extends BytecodeProvider {
    @Override
    public void generateInternal(MethodVisitor mv, Type selfType, Type owner) throws Exception {
        Label LOOP_START = new Label(),
            GREATER_LABEL = new Label(),
            LESS_EQUAL_LABEL = new Label(),
            AFTER_IF=new Label()
        ;

        mv.visitInsn(ICONST_0);

        mv.visitLabel(LOOP_START);

        mv.visitInsn(DUP);
        mv.visitJumpInsn(IFGT, GREATER_LABEL);

        mv.visitLabel(LESS_EQUAL_LABEL);
        mv.visitInsn(ICONST_1);
        mv.visitInsn(IADD);
        mv.visitJumpInsn(GOTO, AFTER_IF);

        mv.visitLabel(GREATER_LABEL);
        mv.visitInsn(ICONST_M1);
        mv.visitInsn(IADD);

//        mv.visitJumpInsn(GOTO,AFTER_IF);
        mv.visitLabel(AFTER_IF);

        mv.visitJumpInsn(GOTO,LOOP_START);

        //Bytecode example
        //ICONST_0
        //L0
        //DUP
        //IF DUP > 0 L1
        //ICONST_1
        //IADD
        //GOTO L2
        //ICONST_M1
        //IADD
        //L2
        //GOTO L0
        //
        //
    }
}
