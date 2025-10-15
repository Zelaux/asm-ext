package asmext.analytics;

import org.jetbrains.annotations.Nullable;

public interface ClassNameResolution {
    /**
     * @return null if not found
     * */
    @Nullable
    byte[] resolveClass(String name);
}
