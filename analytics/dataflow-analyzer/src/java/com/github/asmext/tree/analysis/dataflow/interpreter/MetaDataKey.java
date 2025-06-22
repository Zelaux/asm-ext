package com.github.asmext.tree.analysis.dataflow.interpreter;

import com.github.asmext.tree.analysis.dataflow.value.DataFlowValue;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Represents a unique key used to store and retrieve metadata values of type {@code VALUE_TYPE}.
 * <p>
 * Instances of this class serve as type-safe identifiers for associating metadata entries
 * within data structures such as maps.
 * <p>
 * Keys are assigned a unique integer identifier internally to distinguish between different metadata keys.
 * <p>
 * Two ways to create keys are provided:
 * <ul>
 *   <li>{@link #create()} - creates a new key with a unique identifier; not thread-safe.</li>
 *   <li>{@link #createThreadSafe()} - creates a new key with a unique identifier in a thread-safe manner.</li>
 * </ul>
 *
 * @param <VALUE_TYPE> the type of metadata value associated with this key
 *
 * @see DataFlowValue#getMeta(MetaDataKey)
 * @see DataFlowValue#putMeta(MetaDataKey, Object)
 * @see DataFlowValue#getMetaEntries()
 * @see DataFlowValue#metaDataSize()
 */
@EqualsAndHashCode
@ToString
public class MetaDataKey<VALUE_TYPE> {
    private static int keys = 0;
    /**
     * Unique numeric identifier of this key instance used internally to distinguish keys.
     */
    public final int key;

    private MetaDataKey(int key) {this.key = key;}
    /**
     * Creates a new {@code MetaDataKey} with a unique integer identifier in a thread-safe manner.
     *
     * @return a new unique {@code MetaDataKey} instance
     */
    public static <VALUE_TYPE> MetaDataKey<VALUE_TYPE> createThreadSafe() {
        int key;
        synchronized(MetaDataKey.class) {
            key = keys++;
        }
        return new MetaDataKey<>(key);
    }

    /**
     * Creates a new {@code MetaDataKey} with a unique integer identifier.
     * <p><b>Note:</b> This method is not thread-safe and should not be called concurrently.</p>
     *
     * @return a new unique {@code MetaDataKey} instance
     */
    public static <VALUE_TYPE> MetaDataKey<VALUE_TYPE> create() {
        return new MetaDataKey<>(keys++);
    }

}
