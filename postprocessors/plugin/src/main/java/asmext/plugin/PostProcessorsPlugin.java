package asmext.plugin;

import lombok.AllArgsConstructor;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.plugins.jvm.internal.JvmPluginServices;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import static asmext.plugin.Constants.TASK_NAME;

@AllArgsConstructor(onConstructor_ = @Inject)
public class PostProcessorsPlugin implements Plugin<Project> {

    private final JvmPluginServices jvmPluginServices;

    @Override
    public void apply(@NotNull Project project) {

        var configurations = project.getConfigurations();

        var postProcessorsConfiguration = configurations.create(Constants.CONFIGURATION_NAME, it -> {
            it.setTransitive(true);

            it.setVisible(false);
            it.setCanBeConsumed(false);
            it.setCanBeResolved(false);
            it.setCanBeDeclared(true);
            //            it.setVisible(true);
            //            it.setTransitive(true);
            //            it.setCanBeConsumed(false);
            //            it.setCanBeResolved(true);

        });
        var javaExt = project.getExtensions().getByType(JavaPluginExtension.class);
        //        javaExt.getSourceSets().create(Constants.CONFIGURATION_NAME,"")
        var tasks = project.getTasks();
        var classesTask = tasks.named("classes");
        //        ApplicationPlugin
        //        project.getDependencies().add(Constants.CONFIGURATION_NAME, project);


        Configuration compileClasspathConfiguration = configurations.maybeCreate(Constants.CLASSPATH_NAME);
        compileClasspathConfiguration.setVisible(false);
        compileClasspathConfiguration.setCanBeConsumed(false);
        compileClasspathConfiguration.setCanBeResolved(true);
        compileClasspathConfiguration.setCanBeDeclared(false);

        compileClasspathConfiguration.setDescription("Classpath for " + TASK_NAME + ".");

        compileClasspathConfiguration.extendsFrom(postProcessorsConfiguration);
        //        jvmPluginServices.configureAsCompileClasspath(compileClasspathConfiguration);
        jvmPluginServices.configureAsRuntimeClasspath(compileClasspathConfiguration);
        //        .library().apiUsage().withExternalDependencies().preferStandardJVM()
        var postprocessorsTask = tasks.register(TASK_NAME, RunPostProcessors.class, it -> {
            it.dependsOn(classesTask);
            it.setGroup("build");
            it.dependsOn(compileClasspathConfiguration);
        });

        tasks.named("compileJava").configure(it -> it.getInputs().files(compileClasspathConfiguration));

        classesTask.configure(it -> it.finalizedBy(postprocessorsTask));


    }

}
