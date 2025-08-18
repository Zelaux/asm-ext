package asmlib.util;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;

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
            int classNameIndex = readValue(offsets[typeI]);
            int classIdx = readValue(offsets[classNameIndex]);
            int nameAndTypeIdx = readValue(offsets[typeI] + 2);
            int nameIdx = readValue(offsets[nameAndTypeIdx]);
            int descIdx = readValue(offsets[nameAndTypeIdx] + 2);
            String className = utf8s[classIdx];
            String methodName = utf8s[nameIdx];
            String descriptor = utf8s[descIdx];
            list.add(new ClassMethod(className, methodName, descriptor, types[typeI] == INTERFACE_METHOD));
        }

        return list.stream();
    }

    public Stream<ClassField> usedFields() {
        List<ClassField> list = new ArrayList<>();
        for (int typeIdx = 1; typeIdx < maxPoolSize; ++typeIdx) {
            if (types[typeIdx] != FIELD) continue;
            int classIdx = readValue(offsets[typeIdx]);
            int nameAndTypeIndex = readValue(offsets[typeIdx] + 2);
            int nameIdx = readValue(offsets[nameAndTypeIndex]);
            list.add(new ClassField(utf8s[classIdx], utf8s[nameIdx]));
        }
        return list.stream();
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
    @FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
    public static class ClassField {
        String owner;
        String name;
    }

    @SuppressWarnings("ClassCanBeRecord")
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
    @Getter
    public static class ClassMethod {
        String owner;
        String name;
        String descriptor;
        boolean isInterface;

        @Override
        public String toString() {
            return owner + "#" + name + descriptor + (isInterface ? "[i]" : "");
        }
    }
}
