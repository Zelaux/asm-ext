package asmext.jvm.visitor.multi;

import org.objectweb.asm.*;

public class MultiModuleVisitor extends ModuleVisitor{
    private final ModuleVisitor[] visitors;

    protected MultiModuleVisitor(int api, ModuleVisitor... visitors){
        super(api);
        this.visitors = visitors;
    }

    @Override
    public void visitMainClass(String mainClass){
        for(ModuleVisitor visitor : visitors){
            visitor.visitMainClass(mainClass);
        }
    }

    @Override
    public void visitPackage(String packaze){
        for(ModuleVisitor visitor : visitors){
            visitor.visitPackage(packaze);
        }
    }

    @Override
    public void visitRequire(String module, int access, String version){
        for(ModuleVisitor visitor : visitors){
            visitor.visitRequire(module, access, version);
        }
    }

    @Override
    public void visitExport(String packaze, int access, String... modules){
        for(ModuleVisitor visitor : visitors){
            visitor.visitExport(packaze, access, modules);
        }
    }

    @Override
    public void visitOpen(String packaze, int access, String... modules){
        for(ModuleVisitor visitor : visitors){
            visitor.visitOpen(packaze, access, modules);
        }
    }

    @Override
    public void visitUse(String service){
        for(ModuleVisitor visitor : visitors){
            visitor.visitUse(service);
        }
    }

    @Override
    public void visitProvide(String service, String... providers){
        for(ModuleVisitor visitor : visitors){
            visitor.visitProvide(service, providers);
        }
    }

    @Override
    public void visitEnd(){
        for(ModuleVisitor visitor : visitors){
            visitor.visitEnd();
        }
    }
}
