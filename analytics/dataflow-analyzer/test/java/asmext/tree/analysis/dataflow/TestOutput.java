package asmext.tree.analysis.dataflow;

import java.io.File;
import java.nio.file.Path;

public class TestOutput {
    private final static Path path;

    static {
        File file = new File("gitignore/test_output/");
        path = file.toPath();
    }

    public static File subfolder(boolean clear, String folderName) {
        File file = new File(path.toFile(), folderName);
        if(clear) //noinspection ResultOfMethodCallIgnored
            file.delete();
        if(!file.mkdirs()) {
            deleteFileTree(file);
            file.mkdirs();
        }
        return file;
    }

    private static void deleteFileTree(File file) {
        if(file.isFile()) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
            return;
        }
        //noinspection ResultOfMethodCallIgnored
        file.listFiles(x -> {
            deleteFileTree(x);
            return false;
        });
        file.delete();
    }
}
