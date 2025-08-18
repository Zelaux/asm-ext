package asmlib.util;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClassFileMetaData extends ClassFileMetaDataLombok {

    private static final String[] typeToName;
    @SuppressWarnings("FieldCanBeLocal")
    private final String[] typesAsStrings;

    static {
        //noinspection Convert2Lambda
        Map<String, Byte> fields = Arrays.stream(ClassFileMetaDataLombok.class.getDeclaredFields())
                .filter(it -> it.getType() == byte.class).collect(Collectors.toMap(
                        Field::getName, new Function<>() {
                            @Override
                            @SneakyThrows
                            public Byte apply(Field it) {
                                it.setAccessible(true);
                                return (byte) it.get(null);
                            }
                        }
                ));

        int maxValue = fields.values().stream().mapToInt(Byte::intValue).max().orElseThrow();
        typeToName = new String[maxValue + 1];
        for (Map.Entry<String, Byte> entry : fields.entrySet()) {
            typeToName[entry.getValue()] = entry.getKey();
        }

    }

    public ClassFileMetaData(byte[] byteCode) {
        super(byteCode);
        typesAsStrings = typesAsString();
    }

    public Stream<ClassMethod> usedMethods() {
        List<ClassMethod> list = new ArrayList<>();
        for (int typeI = 1; typeI < maxPoolSize; ++typeI) {
            if (!isMethod(typeI)) continue;
            list.add(readMethod(typeI));
        }

        return list.stream();
    }

    protected @NotNull ClassMethod readMethod(int i) {
        int classNameIndex = readValue(offsets[i]);
        int classIdx = readValue(offsets[classNameIndex]);
        int nameAndTypeIdx = readValue(offsets[i] + 2);
        int nameIdx = readValue(offsets[nameAndTypeIdx]);
        int descIdx = readValue(offsets[nameAndTypeIdx] + 2);
        String className = utf8s[classIdx];
        String methodName = utf8s[nameIdx];
        String descriptor = utf8s[descIdx];
        ClassMethod method = new ClassMethod(className, methodName, descriptor, types[i] == INTERFACE_METHOD);
        return method;
    }

    public Stream<ClassField> usedFields() {
        List<ClassField> list = new ArrayList<>();
        for (int typeIdx = 1; typeIdx < maxPoolSize; ++typeIdx) {
            if (types[typeIdx] != FIELD) continue;
            list.add(readField(typeIdx));
        }
        return list.stream();
    }

    protected @NotNull ClassField readField(int typeIdx) {
        int classNameIndexIndex = readValue(offsets[typeIdx]);
        int classIdx = readValue(offsets[classNameIndexIndex]);
        int nameAndTypeIndex = readValue(offsets[typeIdx] + 2);
        int nameIdx = readValue(offsets[nameAndTypeIndex]);
        int descIdx = readValue(offsets[nameAndTypeIndex] + 2);
        ClassField e = new ClassField(utf8s[classIdx], utf8s[nameIdx], utf8s[descIdx]);
        return e;
    }

    public boolean usesClass(Class<?> clazz) {
        return usesClass(clazz.getName().replace('.', '/'));
    }

    public boolean usesClass(String internalName) {
        return findClass(internalName) != -1;
    }

    public Stream<String> usedClasses() {
        List<String> list = new ArrayList<>();
        for (int typeI = 0; typeI < types.length; typeI++) {
            if (types[typeI] != CLASS) continue;
            list.add(utf8s[readValue(offsets[typeI])]);
        }
        return list.stream();
    }

    public boolean usesAnnotation(Class<? extends Annotation> type) {
        return usesAnnotation('L' + type.getName().replace('.', '/') + ';');
    }

    public boolean usesAnnotation(String descriptor) {
        return containsUtf8(descriptor);
    }

    public String[] typesAsString() {
        String[] strings = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            strings[i] = typeToName[types[i]];
        }
        return strings;
    }

    @SuppressWarnings("ClassCanBeRecord")
    @AllArgsConstructor
    @Getter
    @FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
    @EqualsAndHashCode
    public static class ClassField {
        String owner;
        String name;
        String desc;

        @Override
        public String toString() {
            return owner + "." + name + ": " + desc;
        }
    }

    @SuppressWarnings("ClassCanBeRecord")
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
    @Getter
    @EqualsAndHashCode
    public static class ClassMethod {
        String owner;
        String name;
        String descriptor;
        boolean isInterface;

        @Override
        public String toString() {
            return fullDescriptor() + (isInterface ? "[interface]" : "");
        }

        private @NotNull String fullDescriptor() {
            return owner + "#" + name + descriptor;
        }
    }
}
