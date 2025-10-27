package asmlib.transform.context;

public class TransformationContext {
    private final Dependencies runtime, compile;

    public TransformationContext(Dependencies runtime, Dependencies compile) {
        this.runtime = runtime;
        this.compile = compile;
    }

    public Dependencies runtimeDependencies() {
        return runtime;
    }

    public Dependencies compileDependencies() {
        return compile;
    }
}
