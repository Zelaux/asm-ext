package asmlib.transform;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.tree.ClassNode;

public interface TransformationWriter extends Comparable<TransformationWriter> {
    static @Nullable TransformationWriter nodeTransformer(NodeTransformer transformer) {
        return transformer;
    }
    static @Nullable TransformationWriter writeTransformer(WriteTransformer transformer) {
        return transformer;
    }

    @Override
    default int compareTo(@NotNull TransformationWriter o) {
        return Integer.compare(priority(), o.priority());
    }

    default int priority() {
        return 0;
    }

    /**
     * @return null if no transformation needed
     */
    @Nullable
    ClassNode transformClass(ClassNode classNode);

    /**
     * @return null if no transformation needed
     */
    @Nullable
    ClassVisitor createWriteVisitor(String className, ClassVisitor visitor);
    interface NodeTransformer extends TransformationWriter{
        @Override
        @Nullable
        default ClassVisitor createWriteVisitor(String className, ClassVisitor visitor){
            return null;
        }
    }
    interface WriteTransformer extends TransformationWriter{
        @Override
        @Nullable
        default ClassNode transformClass(ClassNode classNode) {
            return null;
        }
    }
}
