package asmext.analytics.controlflow.methodgen;

import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

/**
 * @author Zelaux
 * @since 2025-09
 */
public abstract class CommonTestThing {
    public final Class<?> originalClass;

    protected CommonTestThing(Class<?> originalClass) {this.originalClass = originalClass;}


    protected CommonTestThing(String originalClass) {this(getName(originalClass));}

    @SneakyThrows
    private static @NotNull Class<?> getName(String originalClass) {
        return Class.forName(originalClass);
    }


    public abstract int test(int a, int b, int c, Object... varArgs);

    public String toString() {
        return "source: " + originalClass;
    }

}
