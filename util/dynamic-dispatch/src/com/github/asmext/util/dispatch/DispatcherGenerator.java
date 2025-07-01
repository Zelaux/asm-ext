package com.github.asmext.util.dispatch;

import lombok.Lombok;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.*;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.WeakHashMap;

public class DispatcherGenerator implements Opcodes {


    public static final String generatedPostfix = "$dispatch";
    public static final Object[] EMPTY_OBJECT_ARRAY = {};

    public static <T> T generateDispatcher(Class<T> metaProcessorClass) {
        byte[] bytes = generateDispatcherBytecode(metaProcessorClass);
        String className = metaProcessorClass.getName() + generatedPostfix;
        ByteCodeClassLoader instance = ByteCodeClassLoader.instanceFor(metaProcessorClass.getClassLoader());
        instance.addClass(className, bytes);
        try {
            Class<?> loaded = instance.loadClass(className);
            //noinspection unchecked
            return (T) loaded.getConstructors()[0].newInstance();
        } catch(ClassNotFoundException | InstantiationException | IllegalAccessException |
                InvocationTargetException e) {
            throw Lombok.sneakyThrow(e);
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
        for(Method method : methods) {
            if(method.getAnnotation(DispatchHub.class) == null) continue;
            if(foundM != null)
                throw new RuntimeException("Multiple @DispatchHub (<<" + foundM + ">> and <<" + method + ">>)");
            foundM = method;
        }
        if(foundM == null) throw new RuntimeException("Cannot @DispatchHub method");
        return foundM;
    }

    private static byte[] generateFromReflection(Class<?> clazz, Method dispatchMethod) {
        Method[] methods = clazz.getDeclaredMethods();
        MethodInfo[] infos = new MethodInfo[methods.length];
        int actualSize = 0;
        for(Method m : methods) {
            if(!m.getName().equals(dispatchMethod.getName()) || m.getParameterCount() != dispatchMethod.getParameterCount() || dispatchMethod.equals(m))
                continue;
            MethodInfo method = methodInfo(clazz, m);
            infos[actualSize++] = method;
        }
        if(actualSize != infos.length) infos = Arrays.copyOf(infos, actualSize);
        return generateDispatcher(
            Type.getType(clazz), infos, methodInfo(clazz, dispatchMethod)
        );
    }

    private static MethodInfo methodInfo(Class<?> clazz, Method method) {
        Type type = Type.getType(method);
        return new MethodInfo(
            Type.getType(clazz),
            method.getName(),
            Modifier.isStatic(method.getModifiers()),
            type
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

        var mv = cw.visitMethod(ACC_PUBLIC, dispatcherMethod.name, dispatcherMethod.type.getDescriptor(), null, null);
        mv.visitCode();
        //TODO add sorting
        Type[] dispatcherParameters = dispatcherMethod.parameters;
        final int parametersAmount = dispatcherParameters.length;
        Object[] localNames = new Object[parametersAmount + 1];
        {
            localNames[0] = selfType.getInternalName();
            for(int i = 0; i < parametersAmount; i++) {
                localNames[i + 1] = dispatcherParameters[i].getInternalName();
            }
        }
        {
            final Label[] endLabels = new Label[parametersAmount];

            for(MethodInfo method : methods) {
                Arrays.fill(endLabels, null);
                int extraPop;
                if(method.isStatic) {
                    extraPop = -1;
                } else {
                    extraPop = 0;
                    mv.visitVarInsn(ALOAD, 0);
                }
                Label finalLabel = null;
                for(int i = 0; i < parametersAmount; i++) {
                    int varIndex = dispatcherMethod.realVarIndex(i);
                    Type paramType = method.parameters[i];
                    if(dispatcherParameters[i].equals(paramType)) {
                        loadInsn(mv, paramType, varIndex);
                        continue;
                    }
                    endLabels[i] = addInstanceCheck(mv, varIndex, paramType, endLabels[i]);
                    finalLabel = new Label();
                }
                invokeMethod(mv, method);

                if(finalLabel != null) {
                    //                    mv.visitJumpInsn(GOTO, finalLabel);

                    int counter = 0;
                    for(int i = 0; i < parametersAmount; i++) {
                        Label label = endLabels[i];
                        counter++;
                        if(label == null) continue;
                        mv.visitLabel(label);
                        for(int __i = 0; __i < counter + extraPop; __i++) {
                            mv.visitInsn(POP - 1 + dispatcherParameters[i - __i].getSize());
                        }
                        if(i + 1 < parametersAmount) mv.visitJumpInsn(GOTO, finalLabel);
                    }

                    mv.visitLabel(finalLabel);

                }
            }
        }


        mv.visitVarInsn(ALOAD, 0);
        for(int i = 0; i < parametersAmount; i++) {
            loadInsn(mv, dispatcherParameters[i], dispatcherMethod.realVarIndex(i));
        }
        mv.visitMethodInsn(
            INVOKESPECIAL,
            ownerType.getInternalName(),
            dispatcherMethod.name,
            dispatcherMethod.type.getDescriptor(),
            false
        );

        mv.visitInsn(dispatcherMethod.returnType.getOpcode(IRETURN));

        mv.visitMaxs(parametersAmount * 2, parametersAmount + 1);
        mv.visitEnd();

        return cw.toByteArray();
    }

    private static Type getTypeFromName(String name) {
        return Type.getType('L' + name.replace('.', '/') + ';');
    }

    private static @NotNull String getInternalName(String name) {
        return name.replace('.', '/');
    }

    private static void loadInsn(MethodVisitor mv, Type type, int varIndex) {
        mv.visitVarInsn(type.getOpcode(ILOAD), varIndex);
    }

    private static @NotNull Label addInstanceCheck(MethodVisitor mv, int varIndex, Type type, @Nullable Label jumpLabel) {
        if(jumpLabel == null) jumpLabel = new Label();
        mv.visitVarInsn(ALOAD, varIndex);
        mv.visitTypeInsn(INSTANCEOF, type.getInternalName());
        mv.visitJumpInsn(IFEQ, jumpLabel);
        mv.visitVarInsn(ALOAD, varIndex);
        mv.visitTypeInsn(CHECKCAST, type.getInternalName());
        return jumpLabel;
    }

    private static void invokeMethod(MethodVisitor mv, MethodInfo methodInfo) {
        mv.visitMethodInsn(
            methodInfo.invokeInstruction(),
            methodInfo.owner.getInternalName(),
            methodInfo.name,
            methodInfo.type.getDescriptor(),
            false);

        mv.visitInsn(methodInfo.type.getReturnType().getOpcode(IRETURN));
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

    public record MethodInfo(Type owner, String name, boolean isStatic, Type type, Type[] parameters, Type returnType) {
        public MethodInfo(Type owner, String name, boolean isStatic, Type type) {
            this(owner, name, isStatic, type, type.getArgumentTypes(), type.getReturnType());
        }

        public int invokeInstruction() {
            return isStatic ? Opcodes.INVOKESTATIC : Opcodes.INVOKEVIRTUAL;
        }

        public int realVarIndex(int i) {
            return isStatic ? i : i + 1;
        }
    }

    static class ByteCodeClassLoader extends ClassLoader {
        private static final ByteCodeClassLoader myInstance = new ByteCodeClassLoader(ByteCodeClassLoader.class.getClassLoader());
        private static final WeakHashMap<ClassLoader, WeakReference<ByteCodeClassLoader>> otherLoaders = new WeakHashMap<>();

        protected ByteCodeClassLoader(ClassLoader parent) {
            super(parent);
        }

        private final HashMap<String, byte[]> map = new HashMap<>();

        public static ByteCodeClassLoader instanceFor(ClassLoader classLoader) {
            if(classLoader.getClass() == ByteCodeClassLoader.class) return myInstance;
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
            if(bytecode == null) throw new ClassNotFoundException(name);
            return defineClass(name, bytecode, 0, bytecode.length);
        }
    }
}
