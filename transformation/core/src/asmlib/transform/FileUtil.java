package asmlib.transform;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;

public class FileUtil{


    public static void copyDirectory(File sourceFile, File destinationFile) throws IOException{
        Path source = sourceFile.toPath();
        Path destination = destinationFile.toPath();
        Files.walkFileTree(source, new SimpleFileVisitor<>(){

            @Override
            public @NotNull FileVisitResult preVisitDirectory(Path dir, @NotNull BasicFileAttributes attrs) throws IOException{
                Path targetDir = destination.resolve(source.relativize(dir)); // Создаем относительный путь
                Files.createDirectories(targetDir); // Создаем директорию, если ее нет
                return FileVisitResult.CONTINUE;
            }

            @Override
            public @NotNull FileVisitResult visitFile(Path file, @NotNull BasicFileAttributes attrs) throws IOException{
                Files.copy(file, destination.resolve(source.relativize(file)), StandardCopyOption.REPLACE_EXISTING); // Копируем файл
                return FileVisitResult.CONTINUE;
            }
        });
    }

}
