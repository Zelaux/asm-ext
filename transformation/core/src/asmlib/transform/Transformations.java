package asmlib.transform;

import asmlib.dev.annotations.EntryPoint;
import asmlib.transform.file.FileEntry;
import asmlib.transform.file.FileExtension;
import asmlib.transform.file.FileTree;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Transformations {

    @EntryPoint
    public static void run(File rootFile, TransformationProvider... providers) throws IOException {
        System.out.println("Transformation started");
        FileTree fileTree = new FileTree(rootFile);
        ArrayList<FileEntry> classpath = new ArrayList<>();
        fileTree.visitFiles(it -> {
            if (FileExtension.Class == it.extensionId) {
                classpath.add(it);
            }
        });
        System.out.println("File tree collected");
        File outputFolder = new File(rootFile.getParentFile(), "raw");
        //noinspection ResultOfMethodCallIgnored
        outputFolder.delete();
        FileUtil.copyDirectory(rootFile, outputFolder);


        TransformationPipeline transformationPipeline = new TransformationPipeline(
                providers
        );
        //noinspection StatementWithEmptyBody
        while (transformationPipeline.round(fileTree, classpath)) {

        }
        System.out.println("Done.");
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
        Exception exception = new Exception(text);
        String name = classNode.name;
        for (AbstractInsnNode instruction : method.instructions) {
            if (instruction instanceof LineNumberNode lineNumberNode) {
                exception.setStackTrace(new StackTraceElement[]{
                        new StackTraceElement(name, method.name, classNode.sourceFile, lineNumberNode.line)
                });
                exception.printStackTrace();
                return;
            }
        }
        exception.setStackTrace(new StackTraceElement[]{
                new StackTraceElement(name, method.name, classNode.sourceFile, -1)
        });
        exception.printStackTrace();
    }
}
