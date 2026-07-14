package asmlib.transform.context;

import asmlib.transform.ExtraArguments;

public class TransformationContext {
    private final Dependencies runtime, compile;
    private final ExtraArguments extraArgs;

    public TransformationContext(Dependencies runtime, Dependencies compile, ExtraArguments extraArgs) {
        this.runtime = runtime;
        this.compile = compile;
        this.extraArgs=extraArgs;
    }

    public Dependencies runtimeDependencies() {
        return runtime;
    }

    public Dependencies compileDependencies() {
        return compile;
    }
    public ExtraArguments extraArguments() {
        return extraArgs;
    }
}
