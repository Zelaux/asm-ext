package asmlib.util;

import asmlib.dev.annotations.AsmVersion;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Stream;

public class NodeUtil {
    public static AnnotationNode findAnnotation(@NotNull Stream<AnnotationNode> stream, Class<? extends Annotation> annotationClass) {
        return findAnnotation(stream, Type.getDescriptor(annotationClass));
    }

    public static AnnotationNode findAnnotation(@NotNull Stream<AnnotationNode> stream, String annotationDescriptor) {
        return stream.filter(it -> it.desc.equals(annotationDescriptor)).findFirst().orElse(null);
    }

    @SafeVarargs
    public static AnnotationNode findAnnotation(Class<? extends Annotation> annotationClass, @Nullable List<AnnotationNode>... lists) {return findAnnotation(Type.getDescriptor(annotationClass), lists);}
    @SafeVarargs
    public static AnnotationNode findAnnotation(String annotationDescriptor, @Nullable List<AnnotationNode>... lists) {
        for (@Nullable List<AnnotationNode> list : lists) {
            if (list == null) continue;
            AnnotationNode found = findAnnotation(list, annotationDescriptor);
            if (found != null) return found;
        }
        return null;
    }

    public static AnnotationNode findAnnotation(@Nullable List<AnnotationNode> list, Class<? extends Annotation> annotationClass) {
        if (list == null) return null;
        return findAnnotation(list, Type.getDescriptor(annotationClass));
    }

    public static AnnotationNode findAnnotation(@Nullable List<AnnotationNode> list, String descriptor) {
        if (list == null) return null;
        for (AnnotationNode node : list) {
            if (node.desc.equals(descriptor)) return node;
        }
        return null;
    }

    @NotNull
    public static ClassNode classNode(Class<?> clazz, @AsmVersion int api) throws IOException {
        ClassReader cr = new ClassReader(clazz.getName());
        return classNode(cr, api);
    }

    @NotNull
    public static ClassNode classNode(byte[] bytes, @AsmVersion int api) {
        ClassReader cr = new ClassReader(bytes);
        return classNode(cr, api);
    }

    @NotNull
    public static ClassNode classNode(ClassReader cr, int api) {
        ClassNode rootClass = new ClassNode(api);
        cr.accept(rootClass, 0);
        return rootClass;
    }

    @SneakyThrows
    public static MethodNode methodNode(Method method, @AsmVersion int api) {
        return extractMethod(classNode(method.getDeclaringClass(), api), method.getName(), Type.getMethodDescriptor(method));
    }

    public static MethodNode extractMethod(ClassNode rootClass, String methodName, String methodDesc) throws NoSuchMethodException {
        return rootClass.methods
                .stream()
                .filter(it -> it.name.equals(methodName) && it.desc.equals(methodDesc))
                .findFirst()
                .orElseThrow(() -> new NoSuchMethodException(methodName + methodDesc));
    }
}
