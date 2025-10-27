package asmlib.transform;

import asmlib.transform.context.TransformationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TransformationProvider extends Comparable<TransformationProvider> {
    @Override
    default int compareTo(@NotNull TransformationProvider o) {
        return Integer.compare(priority(), o.priority());
    }

    default int priority() {
        return 0;
    }

    /**
     * @param className See {@link Class#getCanonicalName()}
     * @param context
     */
    boolean shouldAnalyze(String className, TransformationContext context);

    /**
     * @param className See {@link Class#getCanonicalName()}
     * @param context
     * @return null if do not need to write
     */
    @Nullable
    TransformationWriter analyze(String className, LazyByteCodeProvider byteCodeProvider, TransformationContext context);

    default void finishRound(TransformationContext context) {}

    default void beforeRound(TransformationContext context) {}

    boolean needNextRound(TransformationContext context);
}
