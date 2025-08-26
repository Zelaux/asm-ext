package asmlib.transform;

import asmlib.transform.file.FileEntry;
import asmlib.transform.file.FileTree;
import lombok.Lombok;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class TransformationPipeline {
    TransformationProvider[] transformationProviders;
    public boolean doLog;

    public TransformationPipeline(TransformationProvider... transformationProviders) {
        this.transformationProviders = new TransformationProvider[transformationProviders.length];
        System.arraycopy(transformationProviders, 0, this.transformationProviders, 0, transformationProviders.length);
        Arrays.sort(this.transformationProviders);
    }

    private static void saveToFile(File outputOperationClass, byte[] byteArray) throws IOException {
        //noinspection ResultOfMethodCallIgnored
        outputOperationClass.delete();
        //noinspection ResultOfMethodCallIgnored
        outputOperationClass.getParentFile().mkdirs();
        try (FileOutputStream stream = new FileOutputStream(outputOperationClass)) {
            stream.write(byteArray);
        }
    }

    public boolean round(FileTree root, List<FileEntry> classpath) throws IOException {
        for (var provider : transformationProviders) {
            provider.beforeRound();
        }
        var writers = readState(classpath);
        var state = writeState(writers);
        ArrayList<TransformationProvider> providers = new ArrayList<>(transformationProviders.length);
        for (var provider : transformationProviders) {
            provider.finishRound();
            if (provider.needNextRound()) {
                providers.add(provider);
            }
        }
        this.transformationProviders = providers.toArray(TransformationProvider[]::new);
        boolean hasFiles = state.length>0;
        for (FileEntry entry : state) {
            System.out.printf("Class '%s' changed%n", entry.classpathName());
        }
        return hasFiles && this.transformationProviders.length > 0;
    }

    private FileEntry[] writeState(FileWithData[] writers) throws IOException {
        int changedCounter = 0;
        FileEntry[] changedEntries = new FileEntry[writers.length];
        for (var writer : writers) {
            var key = writer.file;
            var transformers = writer.data;
            byte[] allBytes = writer.byteCode;
            String className = key.classpathName();
            @NotNull
            ClassNode classNode = new ClassNode();
            ClassWriter classWriter;
            {
                ClassReader reader = new ClassReader(allBytes);
                reader.accept(classNode, 0);
                classWriter = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            }


            ClassVisitor visitor = classWriter;
            boolean changed = false;
            for (var transformer : transformers) {
                ClassNode transformed = transformer.transformClass(classNode);
                if (transformed != null) {
                    changed = true;
                    classNode = transformed;
                }
                ClassVisitor writeVisitor = transformer.createWriteVisitor(className, visitor);
                if (writeVisitor != null) {
                    changed = true;
                    visitor = writeVisitor;
                }
            }
            if (changed) {
                classNode.accept(visitor);
                changedEntries[changedCounter++] = key;
                saveToFile(key.file, classWriter.toByteArray());
            }
        }
        if (changedCounter == 0) return FileEntry.EMPTY_ARRAY;
        if (changedCounter < changedEntries.length) return Arrays.copyOf(changedEntries, changedCounter);
        return changedEntries;
    }

    @Nullable
    private FileWithData getTransformationWriters(FileEntry fileEntry) {
        String className = fileEntry.classpathName();
        var transformationProviders = Arrays.stream(this.transformationProviders)
                .filter(prov -> prov.shouldAnalyze(className))
                .toArray(TransformationProvider[]::new);

        if (transformationProviders.length == 0) return null;
        TransformationWriter[] writers = new TransformationWriter[transformationProviders.length];
        byte[] allBytes;
        try (FileInputStream fileInputStream = new FileInputStream(fileEntry.file)) {
            allBytes = fileInputStream.readAllBytes();
        } catch (IOException e) {
            throw Lombok.sneakyThrow(e);
        }
        var byteCodeProvider = new LazyByteCodeProvider(() -> Arrays.copyOf(allBytes, allBytes.length));
        int counter = 0;
        for (var transformer : transformationProviders) {
            ClassReader reader = new ClassReader(allBytes);
            byteCodeProvider.reset();
            var writer = transformer.analyze(className, byteCodeProvider);
            if (writer == null) continue;
            writers[counter++] = writer;

        }
        if (counter == 0) return null;
        if (writers.length > counter) writers = Arrays.copyOf(writers, counter);
        return new FileWithData(fileEntry, allBytes, writers);
    }

    public record FileWithData(FileEntry file, byte[] byteCode, TransformationWriter[] data) {}

    ;

    protected @NotNull FileWithData[] readState(List<FileEntry> classpath) throws IOException {
        List<FileWithData> list = new ArrayList<>();
        for(FileEntry fileEntry : classpath) {
            FileWithData transformationWriters = getTransformationWriters(fileEntry);
            if(transformationWriters != null) {
                list.add(transformationWriters);
            }
        }
        return list.toArray(new FileWithData[0]);
    }

}
