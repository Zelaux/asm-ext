package asmlib.transform;

import asmlib.transform.context.Dependencies;
import asmlib.transform.context.Dependency;
import asmlib.transform.context.TransformationContext;
import org.intellij.lang.annotations.Language;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Main {
    static final Pattern classNamePattern = Pattern.compile("\\w[\\d\\w]*(\\.\\w[\\d\\w]*)*(&\\w[\\d\\w]*)*");

    enum State {
        transformations("-t(ransformation)?"),
        dependencies("-d(ependencies)"),
        compileDependencies("-(cd|compileDependencies)"),
        runtimeDependencies("-(rd|runtimeDependencies)"),
        ;
        public final Pattern pattern;
        public final static State[] all = values();
        public final int id = ordinal();

        State(@Language("RegExp") String pattern) {this.pattern = Pattern.compile(pattern);}
    }

    public static void main(String[] args) throws Exception {
        TransformationProvider[] providers = new TransformationProvider[args.length - 1];
        int providerCount = 0;
        State state = State.transformations;
        Dependencies compile=new Dependencies(),runtime=new Dependencies();

        Map<String, Dependency> dependencyCache=new HashMap<>();
        nextArg:
        for(int i = 1; i < args.length; i++) {
            String arg = args[i];
            for(State s : State.all) {
                if(s.pattern.matcher(arg).matches()) {
                    state = s;
                    continue nextArg;
                }
            }
            switch(state) {
                case transformations -> {
                    addProvider(arg, providers, providerCount++);
                }
                case dependencies -> {
                    compile.add(Dependency.fromString(arg, dependencyCache));
                    runtime.add(Dependency.fromString(arg, dependencyCache));
                }
                case compileDependencies -> compile.add(Dependency.fromString(arg, dependencyCache));
                case runtimeDependencies -> runtime.add(Dependency.fromString(arg, dependencyCache));
            }
        }

        providers = Arrays.copyOf(providers, providerCount);
        Transformations.run(new File(args[0]),

            new TransformationContext(runtime,compile), providers);
    }

    private static void addProvider(String arg, TransformationProvider[] providers, int providerCount) throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Class<?> name = Class.forName(arg);
        Constructor<?> constructor = name.getConstructors()[0];
        constructor.setAccessible(true);
        providers[providerCount] = (TransformationProvider) constructor.newInstance();
    }
}
