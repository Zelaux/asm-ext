package asmext.plugin;

import asmlib.transform.TransformationProvider;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.Delegate;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.TaskAction;
import org.gradle.jvm.toolchain.JavaToolchainService;
import org.gradle.process.JavaExecSpec;
import org.gradle.process.internal.ExecActionFactory;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import javax.inject.Inject;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

class RunPostProcessors extends DelegateExec {
    @Getter
    public final JavaExec javaExec = new JavaExec() {

        @Inject
        protected ObjectFactory getObjectFactory() {
            return RunPostProcessors.this.getObjectFactory();
        }

        @Inject
        protected ExecActionFactory getExecActionFactory() {
            return RunPostProcessors.this.getExecActionFactory();
        }

        @Inject
        protected JavaToolchainService getJavaToolchainService() {
            return RunPostProcessors.this.getJavaToolchainService();
        }

        @Inject
        protected ProviderFactory getProviderFactory() {
            return RunPostProcessors.this.getProviderFactory();
        }
    };


    public static final String SERVICES = "META-INF/services/" + TransformationProvider.class.getCanonicalName();
    public static final Class<RunPostProcessors> SELF_CLASS = RunPostProcessors.class;

    @SneakyThrows
    static URI getFile(Class<?> clazz) {
        URL resource = SELF_CLASS.getClassLoader().getResource(clazz.getName().replace('.', '/') + ".class");
        String[] packageParts = clazz.getPackage().getName().split("\\.");
        char separator = '/';
        String inner = resource.getFile();
        if(File.separatorChar == '\\') {
            inner = inner.replace('\\', '/');
        }

        int prev = inner.length() + 1;
        for(int i = 0; i < packageParts.length + 1; i++) {
            prev = inner.lastIndexOf(separator, prev - 1);
        }
        if(resource.getProtocol().equals("jar")) {
            return new URI(inner.substring(0, prev - 1));
        } else {
            return Path.of(inner.substring(1, prev)).toUri();
        }
    }

    @Inject
    public RunPostProcessors() {
        super();
        getMainClass().set(Constants.ASMLIB_TRANSFORM_MAIN);
    }

    @SneakyThrows
    @TaskAction
    public void run() {
        Project project = getProject();
        Configuration config = project
            .getConfigurations()
            .getByName(Constants.CLASSPATH_NAME);
        config.resolve();
        var configuration = config
            .getResolvedConfiguration();

        var artifacts = configuration.getResolvedArtifacts();
        var javaExt = project.getExtensions().getByType(JavaPluginExtension.class);


        ArrayList<String> rawArgs = new ArrayList<>();
        rawArgs.add("");

        classpath(getFile(asmlib.transform.Main.class));
        classpath(getFile(ClassReader.class));
        classpath(getFile(ClassNode.class));
        boolean error=false;
        for(ResolvedArtifact artifact : artifacts) {
            System.out.println(artifact);

            File file = artifact.getFile();
            if(!file.exists()){
                error=true;
                System.err.println(file.getAbsolutePath());
                break;
            }
            JarFile jarFile = new JarFile(file);
            JarEntry entry = jarFile.getJarEntry(SERVICES);
            if(entry != null) {
                for(String s : new String(jarFile.getInputStream(entry).readAllBytes()).split("\n")) {
                    rawArgs.add(s);
                }
            }
            classpath(file);
        }

        if(error)throw null;
        String[] args = rawArgs.toArray(String[]::new);
        for(File main : javaExt.getSourceSets().getByName("main").getOutput().getClassesDirs()) {
            args[0] = main.getAbsolutePath();
            System.out.println(Arrays.toString(args));
            setArgs(List.of(args));
            javaExec.exec();
        }
    }

    @Inject
    protected ObjectFactory getObjectFactory() {
        throw new UnsupportedOperationException();
    }

    @Inject
    protected ExecActionFactory getExecActionFactory() {
        throw new UnsupportedOperationException();
    }

    @Inject
    protected JavaToolchainService getJavaToolchainService() {
        throw new UnsupportedOperationException();
    }

    @Inject
    protected ProviderFactory getProviderFactory() {
        throw new UnsupportedOperationException();
    }
}
