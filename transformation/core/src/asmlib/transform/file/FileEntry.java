package asmlib.transform.file;

import org.jetbrains.annotations.Debug.Renderer;

import java.io.File;

import static asmlib.transform.file.FileTree.add;

@Renderer(text = "this.classpathName()")
@SuppressWarnings("unused")
public class FileEntry {
    public static final FileEntry[] EMPTY_ARRAY = new FileEntry[0];
    public final FileTree parent;
    public final File file;


    public final FileExtension extensionId;
    public final String extension;
    private final String[] name;

    public FileEntry(File file, FileTree parent, String[] prefix) {
        this.file = file;
        this.parent = parent;
        String rawName = file.getName();
        int i = rawName.indexOf('.');
        if (i == -1) {
            this.name = add(prefix, rawName);
            extensionId = FileExtension.No;
            extension = null;
        } else {
            this.name = add(prefix, rawName.substring(0, i));

            String substring = rawName.substring(i + 1);
            extensionId = FileExtension.classify(substring);
            extension = extensionId.intern(substring);
        }
    }

    public String classpathName() {
        return String.join(".", name);
    }
}
