package asmext.analytics.controlflow;

import org.jetbrains.annotations.Debug;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * @author Zelaux
 */
@Debug.Renderer(text = "debugString()", childrenArray = "toArray()", hasChildren = "isAny()")
public class InsnIdxSet {
    private int[] table;
    private boolean[] used;
    private int capacity;
    private int size;

    public InsnIdxSet(int capacity) {
        this.capacity = capacity;
        table = new int[capacity];
        used = new boolean[capacity];
    }

    public InsnIdxSet(InsnIdxSet other) {
        this.capacity = other.capacity;
        this.size = other.size;
        this.table = Arrays.copyOf(other.table, other.table.length);
        this.used = Arrays.copyOf(other.used, other.used.length);
    }

    public int[] toArray() {
        return toArray(new int[size]);
    }

    public int @NotNull [] toArray(int[] ints) {
        IntIterator iterator = iterator();

        for (int i = 0; i < Math.min(ints.length,size); i++) {
            ints[i] = iterator.next();
        }
        return ints;
    }

    public InsnIdxSet copy() {
        return new InsnIdxSet(this);
    }

    private int hash(int key) {
        return (key & 0x7fffffff) % capacity;
    }

    public boolean add(int key) {
        int idx = hash(key);
        while (used[idx]) {
            if (table[idx] == key) return false;
            idx = (idx + 1) % capacity;
        }
        table[idx] = key;
        used[idx] = true;
        size++;
        return true;
    }

    public boolean contains(int key) {
        int idx = hash(key);
        while (used[idx]) {
            if (table[idx] == key) return true;
            idx = (idx + 1) % capacity;
        }
        return false;
    }

    public boolean remove(int key) {
        int idx = hash(key);
        while (used[idx]) {
            if (table[idx] == key) {
                used[idx] = false;
                rehashFrom(idx);
                size--;
                return true;
            }
            idx = (idx + 1) % capacity;
        }
        return false;
    }

    private void rehashFrom(int start) {
        int idx = (start + 1) % capacity;
        while (used[idx]) {
            int key = table[idx];
            used[idx] = false;
            size--;
            add(key);
            idx = (idx + 1) % capacity;
        }
    }

    public int size() {
        return size;
    }

    public IntIterator iterator() {
        return new IntIterator();
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean isAny() {
        return size > 0;
    }

    public boolean isOne() {
        return size == 1;
    }

    public int first() {
        int current = 0;
        while (current < capacity && !used[current]) {
            current++;
        }
        if (current >= capacity) throw new IllegalStateException("No more elements");
        return table[current];
    }

    public String debugString() {
        return "InsnIdxSet[" + size + "]" + toString();
    }

    @Override
    public String toString() {
        if (size == 0) return "[]";
        String[] items = new String[size];
        IntIterator iterator = iterator();
        for (int i = 0; i < items.length; i++) {
            items[i] = iterator.next() + "";
        }
        return "[" + String.join(", ", items) + "]";
    }

    // Кастомный итератор по int
    public class IntIterator {
        private int current = -1;

        public boolean hasNext() {
            int temp = current + 1;
            while (temp < capacity && !used[temp]) {
                temp++;
            }
            return temp < capacity;
        }

        public int next() {
            current++;
            while (current < capacity && !used[current]) {
                current++;
            }
            if (current >= capacity) throw new IllegalStateException("No more elements");
            return table[current];
        }
    }

}

