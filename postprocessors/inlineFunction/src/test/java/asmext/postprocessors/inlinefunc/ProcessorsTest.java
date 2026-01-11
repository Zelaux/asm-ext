package asmext.postprocessors.inlinefunc;

import asmlib.transform.LazyByteCodeProvider;
import asmlib.util.NodeUtil;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

class ProcessorsTest {
    @SneakyThrows
     static byte[] getBytes(Class<?> clazz) {
        try (var stream = clazz.getClassLoader().getResourceAsStream(clazz.getName().replace('.', '/') + ".class")) {
            return stream.readAllBytes();
        }
    }


    public static void main(String[] args) {


    }

}