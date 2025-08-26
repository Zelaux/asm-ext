package asmext.plugin;

import lombok.AllArgsConstructor;
import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileCollection;
import org.gradle.api.jvm.ModularitySpec;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;
import org.gradle.process.*;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public abstract class DelegateExec extends DefaultTask implements JavaExecSpec {

    @Internal
    public abstract JavaExecSpec getJavaExec();


    @Internal
    public ListProperty<String> getJvmArguments() {return getJavaExec().getJvmArguments();}


    @Internal
    public Property<String> getMainModule() {return getJavaExec().getMainModule();}


    @Internal
    public Property<String> getMainClass() {return getJavaExec().getMainClass();}


    @Internal
    public JavaExecSpec setMain(String main) {return getJavaExec().setMain(main);}


    @Internal
    public List<String> getArgs() {return getJavaExec().getArgs();}


    @Internal
    public JavaExecSpec args(Object... args) {return getJavaExec().args(args);}


    @Internal
    public JavaExecSpec args(Iterable<?> args) {return getJavaExec().args(args);}


    @Internal
    public JavaExecSpec setArgs(List<String> args) {return getJavaExec().setArgs(args);}


    @Internal
    public JavaExecSpec setArgs(Iterable<?> args) {return getJavaExec().setArgs(args);}


    @Internal
    public List<CommandLineArgumentProvider> getArgumentProviders() {return getJavaExec().getArgumentProviders();}


    @Internal
    public JavaExecSpec classpath(Object... paths) {return getJavaExec().classpath(paths);}


    @Internal
    public FileCollection getClasspath() {return getJavaExec().getClasspath();}


    @Internal
    public JavaExecSpec setClasspath(FileCollection classpath) {return getJavaExec().setClasspath(classpath);}


    @Internal
    public ModularitySpec getModularity() {return getJavaExec().getModularity();}


    @Internal
    public Map<String, Object> getSystemProperties() {return getJavaExec().getSystemProperties();}


    @Internal
    public void setSystemProperties(Map<String, ?> properties) {getJavaExec().setSystemProperties(properties);}


    @Internal
    public JavaForkOptions systemProperties(Map<String, ?> properties) {return getJavaExec().systemProperties(properties);}


    @Internal
    public JavaForkOptions systemProperty(String name, Object value) {return getJavaExec().systemProperty(name, value);}


    @Internal
    public String getDefaultCharacterEncoding() {return getJavaExec().getDefaultCharacterEncoding();}


    @Internal
    public void setDefaultCharacterEncoding(String defaultCharacterEncoding) {getJavaExec().setDefaultCharacterEncoding(defaultCharacterEncoding);}


    @Internal
    public String getMinHeapSize() {return getJavaExec().getMinHeapSize();}


    @Internal
    public void setMinHeapSize(String heapSize) {getJavaExec().setMinHeapSize(heapSize);}


    @Internal
    public String getMaxHeapSize() {return getJavaExec().getMaxHeapSize();}


    @Internal
    public void setMaxHeapSize(String heapSize) {getJavaExec().setMaxHeapSize(heapSize);}


    @Internal
    public List<String> getJvmArgs() {return getJavaExec().getJvmArgs();}


    @Internal
    public void setJvmArgs(List<String> arguments) {getJavaExec().setJvmArgs(arguments);}


    @Internal
    public void setJvmArgs(Iterable<?> arguments) {getJavaExec().setJvmArgs(arguments);}


    @Internal
    public JavaForkOptions jvmArgs(Iterable<?> arguments) {return getJavaExec().jvmArgs(arguments);}


    @Internal
    public JavaForkOptions jvmArgs(Object... arguments) {return getJavaExec().jvmArgs(arguments);}


    @Internal
    public List<CommandLineArgumentProvider> getJvmArgumentProviders() {return getJavaExec().getJvmArgumentProviders();}


    @Internal
    public FileCollection getBootstrapClasspath() {return getJavaExec().getBootstrapClasspath();}


    @Internal
    public void setBootstrapClasspath(FileCollection classpath) {getJavaExec().setBootstrapClasspath(classpath);}


    @Internal
    public JavaForkOptions bootstrapClasspath(Object... classpath) {return getJavaExec().bootstrapClasspath(classpath);}


    @Internal
    public boolean getEnableAssertions() {return getJavaExec().getEnableAssertions();}


    @Internal
    public void setEnableAssertions(boolean enabled) {getJavaExec().setEnableAssertions(enabled);}


    @Internal
    public boolean getDebug() {return getJavaExec().getDebug();}


    @Internal
    public void setDebug(boolean enabled) {getJavaExec().setDebug(enabled);}


    @Internal
    public JavaDebugOptions getDebugOptions() {return getJavaExec().getDebugOptions();}


    @Internal
    public void debugOptions(Action<JavaDebugOptions> action) {getJavaExec().debugOptions(action);}


    @Internal
    public List<String> getAllJvmArgs() {return getJavaExec().getAllJvmArgs();}


    @Internal
    public void setAllJvmArgs(List<String> arguments) {getJavaExec().setAllJvmArgs(arguments);}


    @Internal
    public void setAllJvmArgs(Iterable<?> arguments) {getJavaExec().setAllJvmArgs(arguments);}


    @Internal
    public JavaForkOptions copyTo(JavaForkOptions options) {return getJavaExec().copyTo(options);}


    @Internal
    public String getExecutable() {return getJavaExec().getExecutable();}


    @Internal
    public void setExecutable(String s) {getJavaExec().setExecutable(s);}


    @Internal
    public void setExecutable(Object o) {getJavaExec().setExecutable(o);}


    @Internal
    public ProcessForkOptions executable(Object o) {return getJavaExec().executable(o);}


    @Internal
    public File getWorkingDir() {return getJavaExec().getWorkingDir();}


    @Internal
    public void setWorkingDir(File file) {getJavaExec().setWorkingDir(file);}


    @Internal
    public void setWorkingDir(Object o) {getJavaExec().setWorkingDir(o);}


    @Internal
    public ProcessForkOptions workingDir(Object o) {return getJavaExec().workingDir(o);}


    @Internal
    public Map<String, Object> getEnvironment() {return getJavaExec().getEnvironment();}


    @Internal
    public void setEnvironment(Map<String, ?> map) {getJavaExec().setEnvironment(map);}


    @Internal
    public ProcessForkOptions environment(Map<String, ?> map) {return getJavaExec().environment(map);}


    @Internal
    public ProcessForkOptions environment(String s, Object o) {return getJavaExec().environment(s, o);}


    @Internal
    public ProcessForkOptions copyTo(ProcessForkOptions processForkOptions) {return getJavaExec().copyTo(processForkOptions);}


    @Internal
    public BaseExecSpec setIgnoreExitValue(boolean b) {return getJavaExec().setIgnoreExitValue(b);}


    @Internal
    public boolean isIgnoreExitValue() {return getJavaExec().isIgnoreExitValue();}


    @Internal
    public BaseExecSpec setStandardInput(InputStream inputStream) {return getJavaExec().setStandardInput(inputStream);}


    @Internal
    public InputStream getStandardInput() {return getJavaExec().getStandardInput();}


    @Internal
    public BaseExecSpec setStandardOutput(OutputStream outputStream) {return getJavaExec().setStandardOutput(outputStream);}


    @Internal
    public OutputStream getStandardOutput() {return getJavaExec().getStandardOutput();}


    @Internal
    public BaseExecSpec setErrorOutput(OutputStream outputStream) {return getJavaExec().setErrorOutput(outputStream);}


    @Internal
    public OutputStream getErrorOutput() {return getJavaExec().getErrorOutput();}


    @Internal
    public List<String> getCommandLine() {return getJavaExec().getCommandLine();}
}
