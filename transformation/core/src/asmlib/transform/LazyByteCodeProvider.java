package asmlib.transform;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.util.function.Supplier;

public final class LazyByteCodeProvider {

    private static final Object UNINIT = new Object();

    public LazyByteCodeProvider(Supplier<byte[]> provider) {
        this.provider = provider;
    }

    private final Supplier<byte[]> provider;
    private Object array = UNINIT;

    public void reset() {
        array = UNINIT;
    }

    public byte[] getCloned() {
        Object array1 = array;
        if (array1 == UNINIT) array = array1 = provider.get();
        return (byte[]) array1;
    }

    public ClassReader createReader() {
        return new ClassReader(getCloned());
    }

    public ClassNode createClassNode() {
        ClassReader reader = createReader();
        ClassNode node = new ClassNode();
        reader.accept(node, 0);
        return node;
    }
}
