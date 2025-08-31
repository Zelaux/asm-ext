package asmext.postprocessors.dynamicdispatch;

import asmlib.transform.AbstractClassFileTransformer;
import asmlib.transform.LazyByteCodeProvider;
import asmlib.transform.TransformationWriter;
import asmlib.transform.Transformations;
import asmlib.util.ClassFileMetaData;
import asmlib.util.NodeUtil;
import asmext.util.dispatch.DefaultVariantKind;
import asmext.util.dispatch.DispatchHub;
import asmext.util.dispatch.DispatcherGenerator;
import asmext.util.dispatch.MethodInfo;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.*;

public class Main extends AbstractClassFileTransformer {

    public Main() {
        super(1);
    }

    @Override
    public boolean shouldAnalyze(String className) {
        return true;
    }

    @Override
    public @Nullable TransformationWriter analyze(String className, LazyByteCodeProvider byteCodeProvider) {
        var metaData = new ClassFileMetaData(byteCodeProvider.getCloned());
        if(!metaData.usesAnnotation(DispatchHub.class)) return null;
        var classNode = byteCodeProvider.createClassNode();
        for(MethodNode method : classNode.methods) {
            var annotation = NodeUtil.findAnnotation(DispatchHub.class, method.invisibleAnnotations, method.visibleAnnotations);
            if(annotation != null) return TransformationWriter.nodeTransformer(this::transformClass);
        }
        return null;
    }

    private static final boolean inlineDefault = false;

    private @Nullable ClassNode transformClass(ClassNode classNode) {
        var roots = new HashSet<MethodNodeKey>();
        var collect = new HashMap<MethodNodeKey, ArrayList<MethodNode>>();
        List<MethodNode> methods = classNode.methods;

        for(int i = 0; i < methods.size(); i++) {
            MethodNode method = methods.get(i);
            collect.computeIfAbsent(new MethodNodeKey(method), k -> new ArrayList<>()).add(method);
            var annotation = NodeUtil.findAnnotation(DispatchHub.class, method.invisibleAnnotations, method.visibleAnnotations);
            if(annotation == null) continue;
            if(!roots.add(new MethodNodeKey(method))) {
                Transformations.showError(classNode, method, "Duplicated @DispatchHub for save signature method");
                continue;
            }
            if(inlineDefault) {
                methods.remove(i--);
            } else {
                //                method.invisibleAnnotations.remove(annotation);
                method.visibleAnnotations.remove(annotation);
            }
        }
        for(MethodNodeKey root : roots) {
            var nodes = collect.get(root);

            Type type = typeFromNode(classNode);

            MethodInfo[] methods_ = new MethodInfo[nodes.size() - 1];
            int j = 0;
            for(MethodNode methodNode : nodes) {
                if(methodNode == root.node) continue;
                methods_[j++] = methodInfo(classNode, methodNode);
            }

            var dispatcherMethod = methodInfo(classNode, root.node);
            root.node.name += "$default";

            DispatcherGenerator.generateDispatcherMethodBody(
                classNode, type, type, methods_, dispatcherMethod, inlineDefault ? DefaultVariantKind.inlineBytecode : DefaultVariantKind.invokeOther
            );
        }


        return classNode;
    }

    private static Type typeFromNode(ClassNode classNode) {
        return Type.getObjectType(classNode.name);
    }

    private static MethodInfo methodInfo(ClassNode clazz, MethodNode method) {
        return MethodInfo.make(clazz, method);
    }


    @AllArgsConstructor
    static class MethodNodeKey {
        MethodNode node;

        @Override
        public final boolean equals(Object o) {
            if(!(o instanceof MethodNodeKey that)) return false;

            return getSize() == that.getSize() && Objects.equals(node.name, that.node.name);
        }

        private int getSize() {
            if(node.parameters == null) {
                return Type.getMethodType(node.desc).getArgumentCount();
            }
            return node.parameters.size();
        }

        @Override
        public int hashCode() {
            int result = Objects.hashCode(node.name);
            result = 31 * result + getSize();
            return result;
        }
    }
}
