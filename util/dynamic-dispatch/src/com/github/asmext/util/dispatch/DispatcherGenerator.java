package com.github.asmext.util.dispatch;

import lombok.Lombok;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.WeakHashMap;
import java.util.function.Supplier;

public class DispatcherGenerator implements Opcodes {


    public static final String generatedPostfix = "$dispatch";
    public static final Object[] EMPTY_OBJECT_ARRAY = {};

    public static <T> T generateDispatcher(Class<T> metaProcessorClass) {
        return generateDispatcherSupplier(metaProcessorClass).get();
    }

    public static <T> Supplier<T> generateDispatcherSupplier(Class<T> metaProcessorClass) {
        byte[] bytes = generateDispatcherBytecode(metaProcessorClass);
        String className = metaProcessorClass.getName() + generatedPostfix;
        ByteCodeClassLoader instance = ByteCodeClassLoader.instanceFor(metaProcessorClass.getClassLoader());
        instance.addClass(className, bytes);
        try {
            Class<?> loaded = instance.loadClass(className);
            return new MySupplier<>(loaded.getConstructors()[0]);
        } catch (Exception e) {
            throw Lombok.sneakyThrow(e);
        } catch (ClassFormatError e) {
            throw e;
        }
    }

    public static <T> byte[] generateDispatcherBytecode(Class<T> metaProcessorClass) {
        Method foundM = findDispatchMethod(metaProcessorClass);

        return generateFromReflection(metaProcessorClass, foundM);
    }

    private static <T> @NotNull Method findDispatchMethod(Class<T> metaProcessorClass) {
        Method[] methods = metaProcessorClass.getDeclaredMethods();
        //        ArrayList<Method> hubs=null;
        //TODO dispatch for multiple methods
        Method foundM = null;
        for (Method method : methods) {
            if (method.getAnnotation(DispatchHub.class) == null) continue;
            if (foundM != null)
                throw new RuntimeException("Multiple @DispatchHub (<<" + foundM + ">> and <<" + method + ">>)");
            foundM = method;
        }
        if (foundM == null) throw new RuntimeException("Cannot @DispatchHub method");
        return foundM;
    }

    private static byte[] generateFromReflection(Class<?> clazz, Method dispatchMethod) {
        Method[] methods = clazz.getDeclaredMethods();
        MethodInfo[] infos = new MethodInfo[methods.length];
        int actualSize = 0;
        for (Method m : methods) {
            if (!m.getName().equals(dispatchMethod.getName()) || m.getParameterCount() != dispatchMethod.getParameterCount() || dispatchMethod.equals(m))
                continue;
            MethodInfo method = MethodInfo.make(clazz, m);
            infos[actualSize++] = method;
        }
        if (actualSize != infos.length) infos = Arrays.copyOf(infos, actualSize);
        return generateDispatcher(
                Type.getType(clazz), infos, MethodInfo.make(clazz, dispatchMethod)
        );
    }

    public static byte[] generateDispatcher(
            Type ownerType, MethodInfo[] methods,
            MethodInfo dispatcherMethod
    ) {
        var cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        //        var cw = new ClassWriter(0);
        Type selfType = getTypeFromName(ownerType.getClassName() + generatedPostfix);
        cw.visit(V17, ACC_PUBLIC | ACC_SUPER, selfType.getInternalName(), null, ownerType.getInternalName(), null);

        cw.visitSource("MetaProcessor$disp.java", null);
        generateConstructor(cw, selfType, ownerType);

        generateDispatcherMethodBody(cw, selfType, ownerType, methods, dispatcherMethod, DefaultVariantKind.invokeSuper);

        return cw.toByteArray();
    }

    public static void generateDispatcherMethodBody(ClassVisitor cw, Type selfType, Type ownerType, MethodInfo[] methods, MethodInfo dispatcherMethod, DefaultVariantKind kind) {
        var mv = cw.visitMethod(ACC_PUBLIC,
                dispatcherMethod.name,
                dispatcherMethod.descriptor,
                dispatcherMethod.signature,
                dispatcherMethod.exceptions);
        mv.visitCode();
        //TODO add sorting
        Type[] dispatcherParameters = dispatcherMethod.parameters;
        final int parametersAmount = dispatcherParameters.length;
        Object[] localNames = new Object[parametersAmount + 1];
        {
            localNames[0] = selfType.getInternalName();
            for (int i = 0; i < parametersAmount; i++) {
                localNames[i + 1] = dispatcherParameters[i].getInternalName();
            }
        }
        {
            final Label[] endLabels = new Label[parametersAmount];

            for (MethodInfo method : methods) {
                Arrays.fill(endLabels, null);
                int extraPop;
                if (method.methodKind.isStatic()) {
                    extraPop = -1;
                } else {
                    extraPop = 0;

                }
                Label finalLabel = parametersAmount == 0 ? null : new Label();

                for (int i = 0; i < parametersAmount; i++) {
                    int varIndex = dispatcherMethod.realVarIndex(i);
                    Type paramType = method.parameters[i];
                    if (dispatcherParameters[i].equals(paramType)) {
//                        loadInsn(mv, paramType, varIndex);
                        continue;
                    }
                    addInstanceCheck(mv, varIndex, paramType, finalLabel);
                }
                if (extraPop == 0) {
                    mv.visitVarInsn(ALOAD, 0);
                }
                for (int i = 0; i < parametersAmount; i++) {
                    int varIndex = dispatcherMethod.realVarIndex(i);
                    Type paramType = method.parameters[i];
                    if (paramType.equals(dispatcherParameters[i])) {
                        loadInsn(mv, paramType, varIndex);
                    } else {
                        addLoadCast(mv, varIndex, paramType);
                    }
                }
                invokeMethod(mv, method);
                // Null if parametersAmount == 0
                if (finalLabel != null) mv.visitLabel(finalLabel);
            }
        }


        DefaultVariantKind kind0 = kind;
        kind0.process(mv, ownerType, dispatcherParameters, dispatcherMethod);

        mv.visitMaxs(parametersAmount * 2, parametersAmount + 1);
        mv.visitEnd();
    }

    private static Type getTypeFromName(String name) {
        return Type.getType('L' + name.replace('.', '/') + ';');
    }

    private static @NotNull String getInternalName(String name) {
        return name.replace('.', '/');
    }

    static void loadInsn(MethodVisitor mv, Type type, int varIndex) {
        mv.visitVarInsn(type.getOpcode(ILOAD), varIndex);
    }

    private static @NotNull Label addInstanceCheck(MethodVisitor mv, int varIndex, Type type, @Nullable Label jumpLabel) {
        if (jumpLabel == null) jumpLabel = new Label();
        mv.visitVarInsn(ALOAD, varIndex);
        mv.visitTypeInsn(INSTANCEOF, type.getInternalName());
        mv.visitJumpInsn(IFEQ, jumpLabel);
        return jumpLabel;
    }

    private static void addLoadCast(MethodVisitor mv, int varIndex, Type type) {
        mv.visitVarInsn(ALOAD, varIndex);
        mv.visitTypeInsn(CHECKCAST, type.getInternalName());
    }

    private static void invokeMethod(MethodVisitor mv, MethodInfo methodInfo) {
        mv.visitMethodInsn(
                methodInfo.invokeInstruction(),
                methodInfo.owner.getInternalName(),
                methodInfo.name,
                methodInfo.descriptor,
                methodInfo.methodKind.isInterface());

        mv.visitInsn(methodInfo.returnType.getOpcode(IRETURN));
    }

    public static void load() {
        //Loads class and invoked <clinit>
    }

    static void generateConstructor(ClassWriter classWriter, Type type, Type superType) {
        var mv = classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        var label0 = new Label();
        mv.visitLabel(label0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, superType.getInternalName(), "<init>", "()V", false);
        mv.visitInsn(RETURN);
        var label1 = new Label();
        mv.visitLabel(label1);
        mv.visitLocalVariable("this", type.getDescriptor(), null, label0, label1, 0);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    public static InputStream getByteCodeAsStream(Class<?> type) {
        ClassLoader classLoader = type.getClassLoader();
        if (!(classLoader instanceof ByteCodeClassLoader loader)) return null;
        return new ByteArrayInputStream(loader.map.get(type.getName()));
    }

    static class ByteCodeClassLoader extends ClassLoader {
        private static final ByteCodeClassLoader myInstance = new ByteCodeClassLoader(ByteCodeClassLoader.class.getClassLoader());
        private static final WeakHashMap<ClassLoader, WeakReference<ByteCodeClassLoader>> otherLoaders = new WeakHashMap<>();
        public static final String CLASS_SUFFIX = ".class";

        protected ByteCodeClassLoader(ClassLoader parent) {
            super(parent);
        }

        @Override
        public @Nullable InputStream getResourceAsStream(String name) {
            if (name.endsWith(CLASS_SUFFIX)) {

                byte[] bytes = map.get(name.replace('/', '.').substring(0, name.length() - CLASS_SUFFIX.length()));
                if (bytes != null) return new ByteArrayInputStream(bytes);
            }
            return super.getResourceAsStream(name);
        }

        private final HashMap<String, byte[]> map = new HashMap<>();

        public static ByteCodeClassLoader instanceFor(ClassLoader classLoader) {
            if (classLoader.getClass() == ByteCodeClassLoader.class) return myInstance;
            return otherLoaders
                    .computeIfAbsent(classLoader, x -> new WeakReference<>(new ByteCodeClassLoader(x)))
                    .get();
        }

        public void addClass(String name, byte[] bytecode) {
            map.put(name, bytecode);
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            byte[] bytecode = map.get(name);
            if (bytecode == null) throw new ClassNotFoundException(name);
            return defineClass(name, bytecode, 0, bytecode.length);
        }
    }

    private static class MySupplier<T> implements Supplier<T> {
        private final Constructor<?> constructor;

        public MySupplier(Constructor<?> constructor) {this.constructor = constructor;}

        @Override
        @SneakyThrows
        public T get() {
            //noinspection unchecked

            return (T) constructor.newInstance();
        }
    }
}
