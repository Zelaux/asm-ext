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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class FIleChooserHook {
    @SneakyThrows
    public static void chooseFile(JFrame frame, String windowName, @Nullable FileNameExtensionFilter filter, FileConsumer fileConsumer, Runnable canceled) {
        File path = Vars.myPath;
        String[] existenFilePath = null;
        if (Vars.openQueueFile.exists()) {
            String string = new String(Utils.readBytes(Vars.openQueueFile), StandardCharsets.UTF_8);
            existenFilePath = string.split("\r?\n\r?");

            path = new File(existenFilePath[existenFilePath.length - 1]);
            if (existenFilePath.length > 127) {
                String[] newLines = new String[127];
                int offset = existenFilePath.length - 127;
                System.arraycopy(existenFilePath, offset, newLines, 0, 127);
                try (FileOutputStream stream = new FileOutputStream(Vars.openQueueFile)) {
                    stream.write(String.join("\n", newLines).getBytes(StandardCharsets.UTF_8));
                }
                existenFilePath = newLines;
            }
        }
        if (System.getProperty("os.name").toLowerCase().contains("win")) {

            FileExtension fileExtension = new FileExtension(filter.getDescription(), filter.getExtensions());
            String string = JWindowsFileDialog.showOpenDialog(frame, windowName, path.getPath(), fileExtension);
            if (string == null) {
                canceled.run();
            } else {
                if (fileConsumer.consume(new File(string))) {
                    processFilePath(string, existenFilePath);
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
                processFilePath(file.getAbsolutePath(), existenFilePath);
            }
        } else {
            canceled.run();
        }
    }

    private static void processFilePath(String string, String[] existenFilePath) throws IOException {
        if (existenFilePath == null) {
            try (FileOutputStream stream = new FileOutputStream(Vars.openQueueFile, true)) {
                stream.write('\n');
                stream.write(string.getBytes(StandardCharsets.UTF_8));
            }
            return;
        }
        var set = new HashMap<String, Integer>(existenFilePath.length);

        for (int i = 0; i < existenFilePath.length; i++) {
            String path = existenFilePath[i];
            set.put(path, i);

        }
        set.put(string, existenFilePath.length);

        try (FileOutputStream stream = new FileOutputStream(Vars.openQueueFile)) {
            stream.write(set.entrySet().stream()
                    .sorted(Comparator.comparingInt(Map.Entry::getValue))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.joining("\n"))
                    .getBytes(StandardCharsets.UTF_8));
        }
    }

    public interface FileConsumer {
        boolean consume(File file) throws Throwable;
    }
}
