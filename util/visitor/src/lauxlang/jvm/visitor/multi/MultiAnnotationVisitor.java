package asmext.jvm.visitor.multi;

import org.objectweb.asm.*;

public class MultiAnnotationVisitor extends AnnotationVisitor{
    @SuppressWarnings("CanBeFinal")
    public AnnotationVisitor[] visitors;

    public MultiAnnotationVisitor(int api, AnnotationVisitor[] visitors){
        super(api);
        this.visitors = visitors;
    }

    @Override
    public void visit(String name, Object value){
        for(AnnotationVisitor visitor : visitors){
            visitor.visit(name, value);
        }
    }

    @Override
    public void visitEnum(String name, String descriptor, String value){
        for(AnnotationVisitor visitor : visitors){
            visitor.visitEnum(name, descriptor, value);
        }
    }

    @Override
    public AnnotationVisitor visitAnnotation(String name, String descriptor){
        AnnotationVisitor[] newGen = new AnnotationVisitor[visitors.length];
        for(int i = 0; i < visitors.length; i++){
            newGen[i] = visitors[i].visitAnnotation(name, descriptor);
        }
        return new MultiAnnotationVisitor(api, newGen);
    }

    @Override
    public AnnotationVisitor visitArray(String name){
        AnnotationVisitor[] newGen = new AnnotationVisitor[visitors.length];
        for(int i = 0; i < visitors.length; i++){
            newGen[i] = visitors[i].visitArray(name);
        }
        return new MultiAnnotationVisitor(api, newGen);
    }

    @Override
    public void visitEnd(){
        for(AnnotationVisitor visitor : visitors){
            visitor.visitEnd();
        }
    }
}
