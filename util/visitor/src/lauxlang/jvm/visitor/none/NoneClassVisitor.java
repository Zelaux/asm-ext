package asmext.jvm.visitor.none;

import org.objectweb.asm.*;

@SuppressWarnings("unused")
public class NoneClassVisitor extends ClassVisitor{
    public NoneClassVisitor(int api){
        super(api);
    }

    public NoneClassVisitor(int api, ClassVisitor classVisitor){
        super(api, classVisitor);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces){

    }

    @Override
    public void visitSource(String source, String debug){

    }

    @Override
    public ModuleVisitor visitModule(String name, int access, String version){
        return null;
    }

    @Override
    public void visitNestHost(String nestHost){

    }

    @Override
    public void visitOuterClass(String owner, String name, String descriptor){

    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible){
        return null;
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible){
        return null;
    }

    @Override
    public void visitAttribute(Attribute attribute){

    }

    @Override
    public void visitNestMember(String nestMember){

    }

    @Override
    public void visitPermittedSubclass(String permittedSubclass){

    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access){

    }

    @Override
    public RecordComponentVisitor visitRecordComponent(String name, String descriptor, String signature){
        return null;
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value){
        return null;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions){
        return null;
    }

    @Override
    public void visitEnd(){

    }
}
