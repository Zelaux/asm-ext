package asmlib.transform;

import asmlib.dev.annotations.EntryPoint;
import asmlib.transform.context.TransformationContext;
import asmlib.transform.file.FileEntry;
import asmlib.transform.file.FileExtension;
import asmlib.transform.file.FileTree;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Transformations {

    @EntryPoint
    public static void run(File rootFile, TransformationContext context, TransformationProvider... providers) throws IOException {
        System.out.println("Transformation started.");
        FileTree fileTree = new FileTree(rootFile);
        ArrayList<FileEntry> classpath = new ArrayList<>();
        fileTree.visitFiles(it -> {
            if(FileExtension.Class == it.extensionId) {
                classpath.add(it);
            }
        });
        System.out.println("File tree collected.");
        File outputFolder = new File(rootFile.getParentFile(), "raw");
        //noinspection ResultOfMethodCallIgnored
        outputFolder.delete();
        FileUtil.copyDirectory(rootFile, outputFolder);


        TransformationPipeline transformationPipeline = new TransformationPipeline(
            providers
        );
        //noinspection StatementWithEmptyBody
        int i = 1;
        do {
            System.out.printf("Round %d%n", i++);
        } while(transformationPipeline.round(fileTree, classpath, context));
        System.out.println("\nDone.");
    }


    public static void showError(StackTraceElement element, String text) {
        Exception exception = new Exception(text);
        exception.setStackTrace(new StackTraceElement[]{element});
        exception.printStackTrace();
    }

    public static void showError(ClassNode classNode, MethodNode method, String text) {
        /*
        * ROOT_PATH\asmlib\transform\Transformations.java:46: error: <identifier> expected
                lineNumberNode.
                               ^
        * */
        Exception exception = createErrorException(classNode, method, text);
        exception.printStackTrace();
    }

    public static @NotNull Exception createErrorException(ClassNode classNode, MethodNode method, String text) {
        Exception exception = new Exception(text);
        exception.setStackTrace(new StackTraceElement[]{
            createErrorElement(classNode, method)
        });
        return exception;
    }

    public static StackTraceElement createErrorElement(ClassNode classNode, MethodNode method) {
        String name = classNode.name;
        for(AbstractInsnNode instruction : method.instructions) {
            if(instruction instanceof LineNumberNode lineNumberNode) {
                return new StackTraceElement(name, method.name, classNode.sourceFile, lineNumberNode.line);

            }
        }
        return new StackTraceElement(name, method.name, classNode.sourceFile, -1);
    }
}
