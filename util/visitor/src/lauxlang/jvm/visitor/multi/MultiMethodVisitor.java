package asmext.jvm.visitor.multi;

import org.jetbrains.annotations.*;
import org.objectweb.asm.*;

import java.util.*;

public class MultiMethodVisitor extends MethodVisitor{
    private final MethodVisitor[] delegates;
    private final Map<Label, Label>[] labelToLabel;

    public MultiMethodVisitor(int api, MethodVisitor... delegates){
        super(api);
        this.delegates = delegates;
        //noinspection unchecked
        labelToLabel = new Map[delegates.length];
        for(int i = 0; i < labelToLabel.length; i++){
            labelToLabel[i]=new HashMap<>();
        }
    }

    private Label[] labelList(Label[] list, int i){
        Label[] labels = new Label[list.length];
        for(int labelIdx = 0; labelIdx < labels.length; labelIdx++){
            labels[labelIdx] = label(i, list[labelIdx]);
        }
        return labels;
    }

    @Override
    public void visitParameter(String name, int access){
        for(MethodVisitor visitor : delegates){
            visitor.visitParameter(name, access);
        }
    }

    @Override
    public AnnotationVisitor visitAnnotationDefault(){
        AnnotationVisitor[] visitors = new AnnotationVisitor[delegates.length];
        for(int i = 0; i < delegates.length; i++){
            visitors[i] = delegates[i].visitAnnotationDefault();
        }
        return new MultiAnnotationVisitor(api, visitors);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible){
        AnnotationVisitor[] visitors = new AnnotationVisitor[delegates.length];
        for(int i = 0; i < delegates.length; i++){
            visitors[i] = delegates[i].visitAnnotation(descriptor, visible);
        }
        return new MultiAnnotationVisitor(api, visitors);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible){
        AnnotationVisitor[] visitors = new AnnotationVisitor[delegates.length];
        for(int i = 0; i < delegates.length; i++){
            visitors[i] = delegates[i].visitTypeAnnotation(typeRef, typePath, descriptor, visible);
        }
        return new MultiAnnotationVisitor(api, visitors);
    }

    @Override
    public void visitAnnotableParameterCount(int parameterCount, boolean visible){
        for(MethodVisitor visitor : delegates){
            visitor.visitAnnotableParameterCount(parameterCount, visible);
        }


    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible){
        AnnotationVisitor[] visitors = new AnnotationVisitor[delegates.length];
        for(int i = 0; i < delegates.length; i++){
            visitors[i] = delegates[i].visitParameterAnnotation(parameter, descriptor, visible);
        }
        return new MultiAnnotationVisitor(api, visitors);
    }

    @Override
    public void visitAttribute(Attribute attribute){
        for(MethodVisitor visitor : delegates){
            visitor.visitAttribute(attribute);
        }


    }

    @Override
    public void visitCode(){
        for(MethodVisitor visitor : delegates){
            visitor.visitCode();
        }
    }

    @Override
    public void visitFrame(int type, int numLocal, Object[] local, int numStack, Object[] stack){
        for(MethodVisitor visitor : delegates){
            visitor.visitFrame(type, numLocal, local, numStack, stack);
        }
    }

    @Override
    public void visitInsn(int opcode){
        for(MethodVisitor visitor : delegates){
            visitor.visitInsn(opcode);
        }
    }

    @Override
    public void visitIntInsn(int opcode, int operand){
        for(MethodVisitor visitor : delegates){
            visitor.visitIntInsn(opcode, operand);
        }
    }

    @Override
    public void visitVarInsn(int opcode, int varIndex){
        for(MethodVisitor visitor : delegates){
            visitor.visitVarInsn(opcode, varIndex);
        }
    }

    @Override
    public void visitTypeInsn(int opcode, String type){
        for(MethodVisitor visitor : delegates){
            visitor.visitTypeInsn(opcode, type);
        }
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor){
        for(MethodVisitor visitor : delegates){
            visitor.visitFieldInsn(opcode, owner, name, descriptor);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor){
        for(MethodVisitor visitor : delegates){
            visitor.visitMethodInsn(opcode, owner, name, descriptor);
        }
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface){
        for(MethodVisitor visitor : delegates){
            visitor.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments){
        for(MethodVisitor visitor : delegates){
            visitor.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
        }
    }

    @Override
    public void visitJumpInsn(int opcode, Label label){
        for(int i = 0; i < delegates.length; i++){
            delegates[i].visitJumpInsn(opcode, label(i, label));
        }
    }

    @Override
    public void visitLabel(Label label){
        for(int i = 0; i < delegates.length; i++){
            delegates[i].visitLabel(label(i, label));
        }
    }

    @NotNull
    private Label label(int index, Label label){
        if(index == 0) return label;
        return labelToLabel[index].computeIfAbsent(label, it -> new Label());
    }

    @Override
    public void visitLdcInsn(Object value){
        for(MethodVisitor visitor : delegates){
            visitor.visitLdcInsn(value);
        }
    }

    @Override
    public void visitIincInsn(int varIndex, int increment){
        for(MethodVisitor visitor : delegates){
            visitor.visitIincInsn(varIndex, increment);
        }
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels){
        for(int i = 0; i < delegates.length; i++){
            delegates[i].visitTableSwitchInsn(min, max, label(i, dflt), labelList(labels, i));
        }
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels){
        for(int i = 0; i < delegates.length; i++){
            delegates[i].visitLookupSwitchInsn(label(i, dflt), keys, labelList(labels, i));
        }
    }

    @Override
    public void visitMultiANewArrayInsn(String descriptor, int numDimensions){
        for(MethodVisitor visitor : delegates){
            visitor.visitMultiANewArrayInsn(descriptor, numDimensions);
        }
    }

    @Override
    public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible){
        AnnotationVisitor[] visitors = new AnnotationVisitor[delegates.length];
        for(int i = 0; i < delegates.length; i++){
            visitors[i] = delegates[i].visitInsnAnnotation(typeRef, typePath, descriptor, visible);
        }
        return new MultiAnnotationVisitor(api, visitors);
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type){
        for(int i = 0; i < delegates.length; i++){
            delegates[i].visitTryCatchBlock(label(i, start), label(i, end), label(i, handler), type);
        }


    }

    @Override
    public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible){
        AnnotationVisitor[] visitors = new AnnotationVisitor[delegates.length];
        for(int i = 0; i < delegates.length; i++){
            visitors[i] = delegates[i].visitTryCatchAnnotation(typeRef, typePath, descriptor, visible);
        }
        return new MultiAnnotationVisitor(api, visitors);
    }

    @Override
    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index){
        for(int i = 0; i < delegates.length; i++){
            delegates[i].visitLocalVariable(name, descriptor, signature, label(i,start), label(i,end), index);
        }


    }

    @Override
    public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start_, Label[] end_, int[] index, String descriptor, boolean visible){
        AnnotationVisitor[] visitors = new AnnotationVisitor[delegates.length];
        for(int i = 0; i < delegates.length; i++){
            visitors[i] = delegates[i].visitLocalVariableAnnotation(typeRef, typePath, labelList(start_, i), labelList(end_, i), index, descriptor, visible);
        }
        return new MultiAnnotationVisitor(api, visitors);
    }

    @Override
    public void visitLineNumber(int line, Label start){
        for(int i = 0; i < delegates.length; i++){
            delegates[i].visitLineNumber(line, label(i, start));
        }
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals){
        for(MethodVisitor visitor : delegates){
            visitor.visitMaxs(maxStack, maxLocals);
        }
    }

    @Override
    public void visitEnd(){
        for(MethodVisitor visitor : delegates){
            visitor.visitEnd();
        }
    }
}
