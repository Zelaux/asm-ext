package asmext.tree.analysis.dataflow.util.analyzer;

import asmext.tree.analysis.dataflow.DupType;
import asmext.tree.analysis.dataflow.TestAsmUtil;
import asmext.tree.analysis.dataflow.util.TypeUtil;
import asmext.tree.analysis.dataflow.util.analyzer.bytecode.BytecodeProvider;
import asmext.tree.analysis.dataflow.util.analyzer.bytecode.InifiniteLoopDumpClass;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.SourceInterpreter;
import org.objectweb.asm.tree.analysis.SourceValue;

/**
 * @author Zelaux
 * @since 2025-09
 */
public class TmpTest {
    static final Type TYPE=Type.getType(TmpTest.class);
    @SneakyThrows
    @Test
    void name() {
        byte[] bytecode = new Prov().generate(TYPE);
        TestAsmUtil.debugSaveByteCode("testThing", bytecode, BytecodeProvider.TARGET_CLASS_NAME);
        ClassNode classNode = new ClassNode();
        new ClassReader(bytecode).accept(classNode,0);
        Analyzer<SourceValue> analyzer = new Analyzer<>(new SourceInterpreter());
        Frame<SourceValue>[] frames = analyzer.analyze(classNode.name, classNode.methods.get(1));
        System.out.println(frames);
    }

    static class Prov extends BytecodeProvider {

        @Override
        public void generateInternal(MethodVisitor mv, Type selfType, Type owner) throws Exception {
            Label if1End = new Label(), if1True = new Label(), if1False = new Label();
            Label if2End = new Label(), if2True = new Label(), if2False = new Label();


            mv.visitVarInsn(ILOAD, 1);
            mv.visitJumpInsn(IFEQ, if1True);

            mv.visitLabel(if1False);
            mv.visitInsn(ICONST_0);
            mv.visitJumpInsn(GOTO, if1End);

            mv.visitLabel(if1True);
            mv.visitLdcInsn(1f);

            mv.visitLabel(if1End);

//            mv.visitInsn(DUP2);
//            mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
//            mv.visitInsn(DUP_X2);
//            mv.visitInsn(POP);
//
//            mv.visitMethodInsn(INVOKESTATIC, "java/io/PrintStream", "println", "(I)V", false);


            mv.visitVarInsn(ILOAD, 1);
            mv.visitJumpInsn(IFEQ, if2True);

            mv.visitLabel(if2False);
            mv.visitInsn(ICONST_2);
            mv.visitInsn(IADD);
            mv.visitJumpInsn(GOTO, if2End);

            mv.visitLabel(if2True);
            mv.visitLdcInsn(3f);
            mv.visitInsn(FADD);
            mv.visitInsn(F2I);

            mv.visitLabel(if2End);

            mv.visitInsn(IRETURN);
        }
    }
}
