package asmext.jvm.visitor.multi;

import org.objectweb.asm.*;

import static asmext.jvm.visitor.multi.MultiVisitors.visitSub;

public class MultiRecordComponentVisitor extends RecordComponentVisitor{
    private final RecordComponentVisitor[] visitors;

    public MultiRecordComponentVisitor(int api, RecordComponentVisitor... visitors){
        super(api);
        this.visitors = visitors;
    }


    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible){
        return visitSub(
            visitors,
            it -> it.visitAnnotation(descriptor, visible),
            AnnotationVisitor[]::new,
            it -> new MultiAnnotationVisitor(api, it)
        );
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible){
        return visitSub(
            visitors,
            it -> it.visitTypeAnnotation(typeRef, typePath, descriptor, visible),
            AnnotationVisitor[]::new,
            it -> new MultiAnnotationVisitor(api, it)
        );
    }

    @Override
    public void visitAttribute(Attribute attribute){
        for(RecordComponentVisitor visitor : visitors){
            visitor.visitAttribute(attribute);
        }
    }

    @Override
    public void visitEnd(){
        for(RecordComponentVisitor visitor : visitors){
            visitor.visitEnd();
        }
    }
}
