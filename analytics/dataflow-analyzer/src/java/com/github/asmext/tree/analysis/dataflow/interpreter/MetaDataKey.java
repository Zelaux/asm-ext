package com.github.asmext.tree.analysis.dataflow.interpreter;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class MetaDataKey<T> {
    private static int keys = 0;
    public final int key;

    private MetaDataKey(int key) {this.key = key;}

    public static <T> MetaDataKey<T> createThreadSafe() {
        int key;
        synchronized(MetaDataKey.class) {
            key = keys++;
        }
        return new MetaDataKey<>(key);
    }

    public static <T> MetaDataKey<T> create() {
        return new MetaDataKey<>(keys++);
    }

}
