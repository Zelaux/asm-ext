package asmext.jvm.visitor.multi;

import org.objectweb.asm.*;

@SuppressWarnings("unused")
public class MultiClassVisitor extends ClassVisitor{
    private final ClassVisitor[] visitors;

    public MultiClassVisitor(int api, ClassVisitor... visitors){
        super(api);
        this.visitors = MultiVisitors.sureNotNull(visitors);
    }


    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces){
        for(ClassVisitor visitor : visitors){
            visitor.visit(version, access, name, signature, superName, interfaces);
        }
    }

    @Override
    public void visitSource(String source, String debug){
        for(ClassVisitor visitor : visitors){
            visitor.visitSource(source, debug);
        }


    }

    @Override
    public ModuleVisitor visitModule(String name, int access, String version){
        return MultiVisitors.visitSub(visitors, it -> it.visitModule(name, access, version), ModuleVisitor[]::new, it -> new MultiModuleVisitor(api, it));
    }

    @Override
    public void visitNestHost(String nestHost){
        for(ClassVisitor visitor : visitors){
            visitor.visitNestHost(nestHost);
        }


    }

    @Override
    public void visitOuterClass(String owner, String name, String descriptor){
        for(ClassVisitor visitor : visitors){
            visitor.visitOuterClass(owner, name, descriptor);
        }


    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible){
        return MultiVisitors.visitSub(
            visitors,
            it -> it.visitAnnotation(descriptor, visible),
            AnnotationVisitor[]::new,
            it -> new MultiAnnotationVisitor(api, it)
        );

    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible){
        return MultiVisitors.visitSub(
            visitors,
            it -> it.visitTypeAnnotation(typeRef, typePath, descriptor, visible),
            AnnotationVisitor[]::new,
            it -> new MultiAnnotationVisitor(api, it)
        );
    }

    @Override
    public void visitAttribute(Attribute attribute){
        for(ClassVisitor visitor : visitors){
            visitor.visitAttribute(attribute);
        }


    }

    @Override
    public void visitNestMember(String nestMember){
        for(ClassVisitor visitor : visitors){
            visitor.visitNestMember(nestMember);
        }


    }

    @Override
    public void visitPermittedSubclass(String permittedSubclass){
        for(ClassVisitor visitor : visitors){
            visitor.visitPermittedSubclass(permittedSubclass);
        }


    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access){
        for(ClassVisitor visitor : visitors){
            visitor.visitInnerClass(name, outerName, innerName, access);
        }


    }

    @Override
    public RecordComponentVisitor visitRecordComponent(String name, String descriptor, String signature){
        return MultiVisitors.visitSub(
            visitors,
            it -> it.visitRecordComponent(name, descriptor, signature),
            RecordComponentVisitor[]::new,
            it -> new MultiRecordComponentVisitor(api, it)
        );
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value){
        return MultiVisitors.visitSub(
            visitors,
            it -> it.visitField(access, name, descriptor, signature, value),
            FieldVisitor[]::new,
            it -> new MultiFieldVisitor(api, it)
        );
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions){
        return MultiVisitors.visitSub(
            visitors,
            it -> it.visitMethod(access, name, descriptor, signature, exceptions),
            MethodVisitor[]::new,
            it -> new MultiMethodVisitor(api, it)
        );

    }

    @Override
    public void visitEnd(){
        for(ClassVisitor visitor : visitors){
            visitor.visitEnd();
        }
    }
}
