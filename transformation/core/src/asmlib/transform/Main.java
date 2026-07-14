package asmlib.transform;

import asmlib.transform.context.Dependencies;
import asmlib.transform.context.Dependency;
import asmlib.transform.context.TransformationContext;
import lombok.SneakyThrows;
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
    public static final Class[] CONSTRUCTOR_PARAM = {TransformationContext.class};

    enum State {
        transformations("-t(ransformation)?"),
        dependencies("-d(ependencies)"),
        compileDependencies("-(cd|compileDependencies)"),
        runtimeDependencies("-(rd|runtimeDependencies)"),
        extraArgs("-extra");
        public final Pattern pattern;
        public final static State[] all = values();
        public final int id = ordinal();

        State(@Language("RegExp") String pattern) {this.pattern = Pattern.compile(pattern);}
    }

    public static void main(String[] args) throws Exception {
        @SuppressWarnings("unchecked")
        Class<? extends TransformationProvider>[] providersClass = new Class[args.length - 1];
        int providerCount = 0;
        State state = State.transformations;
        Dependencies compile = new Dependencies(), runtime = new Dependencies();
        ExtraArguments extraArgs = new ExtraArguments();

        Map<String, Dependency> dependencyCache = new HashMap<>();
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
                    addProvider(arg, providersClass, providerCount++);
                }
                case dependencies -> {
                    compile.add(Dependency.fromString(arg, dependencyCache));
                    runtime.add(Dependency.fromString(arg, dependencyCache));
                }
                case compileDependencies -> compile.add(Dependency.fromString(arg, dependencyCache));
                case runtimeDependencies -> runtime.add(Dependency.fromString(arg, dependencyCache));
                case extraArgs -> extraArgs.add(unescapeExtraArg(arg));
            }
            ;
        }


        var providers = new TransformationProvider[providerCount];

        TransformationContext context = new TransformationContext(runtime, compile, extraArgs);
        for(int i = 0; i < providerCount; i++) {
            providers[i] = createTransformationProvider(providersClass[i], context);
        }
        Transformations.run(
            new File(args[0]),
            context,
            providers
        );
    }

    public static String unescapeExtraArg(String arg) {
        StringBuilder builder = null;
        char[] charArray = arg.toCharArray();
        for(int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            if(builder == null) {
                if(c != '\\') {
                    continue;
                } else {
                    builder = new StringBuilder().append(arg, 0, i);
                }
            }
            builder.append(
                c == '\\' && i + 1 < charArray.length ? charArray[++i] : c
            );
        }
        if(builder == null) return arg;
        return builder.toString();
    }
    public static String escapeExtraArg(String arg) {
        char[] resultArray=new char[arg.length()<<1];
        int currentLen=0;
        char[] charArray = arg.toCharArray();
        for(int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            if(c=='\\' || c=='-'){
                resultArray[currentLen++]='\\';
            }

            resultArray[currentLen++]=c;
        }
        return new String(resultArray,0,currentLen);
    }

    @SneakyThrows
    private static TransformationProvider createTransformationProvider(Class<? extends TransformationProvider> providersClass, TransformationContext context) {
        try {
            for(Constructor<?> constructor : providersClass.getConstructors()) {
                if(!Arrays.equals(constructor.getParameterTypes(), CONSTRUCTOR_PARAM)) {
                    continue;
                }

                constructor.setAccessible(true);

                return (TransformationProvider) constructor.newInstance(context);

            }
            Constructor<? extends TransformationProvider> constructor = providersClass.getConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch(ClassCastException e) {
            throw new RuntimeException("Transformation Provider '%s' does not implements asmlib.transform.TransformationProvider".formatted(providersClass.getName()));
        }
    }

    @SuppressWarnings("rawtypes")
    private static void addProvider(String arg, Class<? extends TransformationProvider>[] providers, int providerCount) throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Class type = Class.forName(arg);
        providers[providerCount] = type;
    }
}
