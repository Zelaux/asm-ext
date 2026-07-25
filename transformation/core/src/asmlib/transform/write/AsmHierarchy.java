package asmlib.transform.write;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

public abstract class AsmHierarchy extends Hierarchy {
    public AsmHierarchy(Hierarchy fallback) {
        super(fallback);
    }

    @Nullable
    public abstract ClassNode locateNode(String canonicalName);

    @Override
    public RawClassMeta findRawMeta(String classCanonicalName) {
        ClassNode node = locateNode(classCanonicalName);
        if(node == null) return null;
        return RawClassMeta.of(node.superName, (node.access & Opcodes.ACC_INTERFACE) != 0);
    }
}
