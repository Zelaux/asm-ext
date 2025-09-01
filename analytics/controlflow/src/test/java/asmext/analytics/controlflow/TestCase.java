package asmext.analytics.controlflow;

/**
 * @author Zelaux
 * @since 2025-09
 */
public record TestCase(int result, int a, int b, int c, Object... args) {
    private static final Object[] EMPTY_ARRAY = new Object[0];

    public TestCase(int result, int a, int b, int c) {
        this(result, a, b, c, EMPTY_ARRAY);
    }
}
