package asmlib.transform;

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

    boolean shouldAnalyze(String className);

    /**
     * @return null if do not need to write
     */
    @Nullable
    TransformationWriter analyze(String className, LazyByteCodeProvider byteCodeProvider);

    default void finishRound() {}

    default void beforeRound() {}

    boolean needNextRound();
}
