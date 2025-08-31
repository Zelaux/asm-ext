package asmext.util.dispatch;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class RunTestUtil implements Opcodes {
    @SneakyThrows
    public static String[] runTest(Class<?> clazz, String[] args, Class<?>... context) {
        var contextClassesSet = Arrays.stream(context).collect(Collectors.toSet());
        {
            var queue = new ArrayDeque<Class<?>>();
            queue.add(clazz);
            while (queue.size() > 0) {
                Class<?> pop = queue.pop();
                for (Class<?> declaredClass : pop.getDeclaredClasses()) {
                    if (contextClassesSet.add(declaredClass)) {
                        queue.add(declaredClass);
                    }
                }
            }
        }
        var contextClasses = contextClassesSet.stream().collect(Collectors.toMap(Class::getName, RunTestUtil::getBytes));

        ClassReader reader = new ClassReader(clazz.getName());
        ClassNode classNode = new ClassNode(Opcodes.ASM9);
        reader.accept(classNode, 0);

        String descriptor = Type.getType(DispatchHub.class).getDescriptor();

        List<MethodNode> methods = classNode.methods;
        int foundIndex = -1;
        var methodMap = new HashMap<String, ArrayList<MethodNode>>();
        loop:
        for (int i = 0; i < methods.size(); i++) {
            MethodNode method = methods.get(i);
            if (foundIndex == -1 && method.visibleAnnotations != null)
                for (AnnotationNode annotation : method.visibleAnnotations) {
                    if (Objects.equals(annotation.desc, descriptor)) {
                        foundIndex = i;
                        continue loop;
                    }
                }

            getNodes(methodMap, method).add(method);

        }
        if (foundIndex == -1) throw new IllegalArgumentException("Cannot find dispatch hub method");

        MethodNode found = methods.get(foundIndex);
        MethodInfo[] methodInfos = getNodes(methodMap, found).stream().map(x -> MethodInfo.make(classNode, x))
                .toArray(MethodInfo[]::new);

        Type selfType = Type.getType(clazz);
        MethodInfo dispactcherMethod = MethodInfo.make(classNode, found);


        found.name += "$default";
        DispatcherGenerator.generateDispatcherMethodBody(classNode,
                selfType,
                selfType,
                methodInfos,
                dispactcherMethod,
                DefaultVariantKind.invokeOther
        );

        {
            MethodNode methodNode = classNode.methods.get(classNode.methods.size() - 1);
            InsnList list = new InsnList();
//            list.add(new FieldInsnNode(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"));
//            list.add(new LdcInsnNode("HAHAAHAHAHA"));
//            list.add(new MethodInsnNode(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V", false));
            list.add(methodNode.instructions);
            methodNode.instructions = list;
        }
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        classNode.accept(writer);
        String className = clazz.getName();


        File folder = new File(ClassLoader.getSystemResource(clazz.getName().replace('.', '/') + ".class").getFile()).getParentFile();
        byte[] classBytes = writer.toByteArray();
        System.out.println(folder.getAbsolutePath());
        try (var stream = new FileOutputStream(new File(folder, clazz.getSimpleName() + "0.class"))) {
            stream.write(classBytes);
        }

        contextClasses.put(className, classBytes);
        var loader = new ClassLoader0("0", clazz.getClassLoader(), contextClasses);
        Method method = loader.findClass(className).getDeclaredMethod("main", String[].class);
        method.setAccessible(true);
        method.invoke(null, (Object) args);


        return args;
    }

    @SneakyThrows
    private static byte[] getBytes(Class<?> x) {
        return ClassLoader.getSystemResourceAsStream(x.getName().replace('.', '/')+".class").readAllBytes();
    }

    private static @NotNull ArrayList<MethodNode> getNodes(HashMap<String, ArrayList<MethodNode>> methodMap, MethodNode found) {
        return methodMap.computeIfAbsent(getKey(found), x -> new ArrayList<>());
    }

    @AllArgsConstructor
    static class ClassLoader0 extends ClassLoader {
        public final Map<String, byte[]> classes;

        public ClassLoader0(String name, ClassLoader parent, Map<String, byte[]> classes) {
            super(name, parent);
            this.classes = classes;
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            byte[] bytes = classes.get(name);
            if (bytes != null) return defineClass(name, bytes, 0, bytes.length);
            return super.findClass(name);
        }

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            return super.loadClass(name);
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {

            synchronized (getClassLoadingLock(name)) {

                byte[] bytes = this.classes.get(name);
                Class<?> loadedClass = findLoadedClass(name);
                if(bytes!=null && loadedClass==null) {
                    loadedClass = findClass(name);
                }
                if(loadedClass!=null){
                    if(resolve)resolveClass(loadedClass);
                    return loadedClass;
                }
            }
            return super.loadClass(name, resolve);
        }
    }

    private static @NotNull String getKey(MethodNode found) {
        return found.name + "\n" + Type.getType(found.desc).getArgumentCount();
    }
}
