package asmext.tools.graph.util;

import asmext.tools.graph.Vars;
import io.github.jacksonbrienen.jwfd.FileExtension;
import io.github.jacksonbrienen.jwfd.JWindowsFileDialog;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

public class FIleChooserHook {
    @SneakyThrows
    public static void chooseFile(JFrame frame, String windowName, @Nullable FileNameExtensionFilter filter, FileConsumer fileConsumer, Runnable canceled) {
        File path = Vars.myPath;
        if (Vars.openQueueFile.exists()) {
            String string = new String(Utils.readBytes(Vars.openQueueFile), StandardCharsets.UTF_8);
            String[] split = string.split("\r?\n\r?");
            path = new File(split[split.length - 1]);
            if (split.length > 127) {
                String[] newLines = new String[127];
                int offset = split.length - 127;
                System.arraycopy(split, offset, newLines, 0, 127);
                try (FileOutputStream stream = new FileOutputStream(Vars.openQueueFile)) {
                    stream.write(String.join("\n", newLines).getBytes(StandardCharsets.UTF_8));
                }
            }
        }
        if (System.getProperty("os.name").toLowerCase().contains("win")) {

            FileExtension fileExtension = new FileExtension(filter.getDescription(), filter.getExtensions());
            String string = JWindowsFileDialog.showOpenDialog(frame, windowName, path.getPath(), fileExtension);
            if (string == null) {
                canceled.run();
            } else {

                if (fileConsumer.consume(new File(string))) {

                    try (FileOutputStream stream = new FileOutputStream(Vars.openQueueFile, true)) {
                        stream.write('\n');
                        stream.write(string.getBytes(StandardCharsets.UTF_8));
                    }
                }
            }
            return;
        }
        JFileChooser chooser = new JFileChooser(path);
        chooser.setFileFilter(filter);
        int result = chooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (fileConsumer.consume(file)) {
                try (FileOutputStream stream = new FileOutputStream(Vars.openQueueFile, true)) {
                    stream.write('\n');
                    stream.write(file.getAbsolutePath().getBytes(StandardCharsets.UTF_8));
                }
            }
        } else {
            canceled.run();
        }
    }

    public interface FileConsumer {
        boolean consume(File file) throws Throwable;
    }
}
