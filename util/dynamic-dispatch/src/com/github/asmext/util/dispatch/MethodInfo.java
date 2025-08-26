package com.github.asmext.util.dispatch;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
@EqualsAndHashCode
@ToString
@Getter
@Builder
public final class MethodInfo {
    Type owner;
    String name;
    MethodKind methodKind;
    String descriptor;
    Type[] parameters;
    Type returnType;
    @Nullable MethodNode bytecode;
    @Nullable
    public String signature;
    @Nullable
    public String[] exceptions;

    public static MethodInfo make(Type owner,
                                  String name,
                                  MethodKind methodKind,
                                  String descriptor,
                                  Type[] parameters,
                                  Type returnType,
                                  @Nullable MethodNode bytecode,
                                  @Nullable String signature,
                                  @Nullable String[] exceptions) {
        return new MethodInfo(owner, name, methodKind, descriptor, parameters, returnType, bytecode, signature, exceptions);
    }


    private static MethodInfo make(Type ownerType,
                                   String name,
                                   MethodKind kind,
                                   Type type,
                                   @Nullable MethodNode method,
                                   @Nullable String signature,
                                   @Nullable List<String> exceptions) {
        return make(ownerType, name, kind, type, method, signature, exceptions == null ? null : exceptions.toArray(new String[0]));
    }

    private static MethodInfo make(Type ownerType,
                                   String name,
                                   MethodKind kind,
                                   Type type,
                                   @Nullable MethodNode method,
                                   @Nullable String signature,
                                   @Nullable String[] exceptions) {
        return make(ownerType,
            name,
            kind,
            type.getDescriptor(),
            type.getArgumentTypes(),
            type.getReturnType(),
            method,
            signature,
            exceptions);
    }

    public static MethodInfo make(ClassNode ownerType, MethodNode method) {
        Type type = Type.getType(method.desc);
        boolean isStatic = Modifier.isStatic(method.access);
        boolean isInterface = (Opcodes.ACC_INTERFACE & ownerType.access) != 0;
        return make(
            Type.getObjectType(ownerType.name),
            method.name,
            isStatic ? MethodKind.Static : isInterface ? MethodKind.Interface : MethodKind.Virtual,
            type,
            method,
            method.signature,
            method.exceptions
        );
    }

    public static MethodInfo make(Class<?> ownerType, Method method) {
        Type type = Type.getType(method);
        boolean isStatic = Modifier.isStatic(method.getModifiers());
        boolean isInterface = ownerType.isInterface();
        Class<?>[] exceptionTypes = method.getExceptionTypes();
        String[] exceptions = exceptionTypes.length == 0 ? null : new String[exceptionTypes.length];
        if(exceptions != null) {
            for(int i = 0; i < exceptions.length; i++) {
                exceptions[i] = Type.getType(exceptionTypes[i]).getInternalName();
            }
        }

        return make(
            Type.getType(ownerType),
            method.getName(),
            isStatic ? MethodKind.Static : isInterface ? MethodKind.Interface : MethodKind.Virtual,
            type,
            null,
            null,
            exceptions
        );
    }

    public int invokeInstruction() {
        return methodKind.instruction;
    }

    public int realVarIndex(int i) {
        return methodKind.isStatic() ? i : i + 1;
    }

}
