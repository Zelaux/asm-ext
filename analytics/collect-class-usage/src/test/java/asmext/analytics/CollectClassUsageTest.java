package asmext.analytics;

import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CollectClassUsageTest {
    @SneakyThrows
    private static byte[] getBytes(String x) {
        InputStream systemResourceAsStream = ClassLoader.getSystemResourceAsStream(x + ".class");
        if (systemResourceAsStream == null || x.startsWith("java/")) return null;
        return systemResourceAsStream.readAllBytes();
    }

    @Test
    void collectAll() {
        HashSet<String> strings = CollectClassUsage.collectAll(CollectClassUsageTest::getBytes, false, internalName(CollectClassUsage.class));
        Set<String> expected = Set.of(
                "asmlib/util/ClassFileMetaData",
                "asmext/analytics/ClassNameResolution",
                "asmext/analytics/CollectClassUsage",
                "asmext/analytics/ByteArrSeq",
                "asmlib/util/ClassFileMetaDataLombok"
        );
        Assertions.assertEquals(expected, strings);
    }

    @Test
    void collectAllReq() {
        HashSet<String> strings = CollectClassUsage.collectAll(CollectClassUsageTest::getBytes, true, internalName(CollectClassUsage.class));
        Set<String> expected = Set.of(
                "asmlib/util/ClassFileMetaData$1",
                "asmlib/util/ClassFileMetaData$ClassMethod",
                "asmlib/util/ClassFileMetaData",
                "asmext/analytics/CollectClassUsage",
                "asmext/analytics/ClassNameResolution",
                "asmext/analytics/ByteArrSeq",
                "asmlib/util/ClassFileMetaData$ClassField",
                "asmlib/util/ClassFileMetaDataLombok"
        );
        Assertions.assertEquals(expected, strings);
    }

    private static @NotNull String internalName(Class<?> collectClassUsageClass) {
        return collectClassUsageClass.getName().replace('.', '/');
    }
}