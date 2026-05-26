package asmext.plugin;

import asmlib.transform.TransformationProvider;
import lombok.Getter;
import lombok.SneakyThrows;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.internal.provider.PropertyFactory;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.*;
import org.gradle.jvm.toolchain.JavaToolchainService;
import org.gradle.process.ExecOperations;
import org.gradle.process.internal.ExecActionFactory;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import javax.inject.Inject;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

abstract class RunPostProcessors extends DelegateExec {

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract ConfigurableFileCollection getMainClassesDirs();
    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract ConfigurableFileCollection getCompileClasspath();

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract ConfigurableFileCollection getRuntimeClasspath();

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
    }
    @Override
    public org.gradle.process.JavaExecSpec getJavaExec() {
        throw new UnsupportedOperationException("Use getExecOperations() instead of getJavaExec()");
    }

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract ConfigurableFileCollection getPostprocessorClasspath();

    @Inject
    protected abstract ExecOperations getExecOperations();

    @SneakyThrows
    @TaskAction
    public void run() {
        var artifacts = getPostprocessorClasspath();


        ArrayList<String> rawArgs = new ArrayList<>();
        rawArgs.add("");

        ArrayList<Object> classPath=new ArrayList<>();
        classPath.add(getFile(asmlib.transform.Main.class));
        classPath.add(getFile(ClassReader.class));
        classPath.add(getFile(ClassNode.class));
        boolean error = false;
        for(var file : artifacts.getFiles()) {
            //            System.out.println(artifact);

            if(!file.exists()) {
                error = true;
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
            classPath.add(file);
        }

        if(error) throw null;


        addFilesToArgs(getCompileClasspath().getFiles(), rawArgs, "-cd");
        addFilesToArgs(getRuntimeClasspath().getFiles(), rawArgs, "-rd");


        String[] args = rawArgs.toArray(String[]::new);


        for(File main : getMainClassesDirs().getFiles()) {
            args[0] = main.getAbsolutePath();
            //            System.out.println(Arrays.toString(args));
            getExecOperations().javaexec(spec -> {
                for(Object o : classPath) {
                    spec.classpath(o);
                }
                spec.getMainClass().set(Constants.ASMLIB_TRANSFORM_MAIN);
                spec.setArgs(List.of(args));
            });
        }
    }
    private static void addFilesToArgs(Set<File> files, ArrayList<String> rawArgs, String optionName) {
        if(!files.isEmpty()) {
            rawArgs.add(optionName);
            for(File file : files) {
                if(file.exists()) {
                    rawArgs.add(file.getAbsolutePath());
                }
            }
        }
    }


}
