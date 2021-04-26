package com.ormoyo.ormoyoutil.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("serial")
public class ListHashMap<K, V> extends LinkedHashMap<K, V> {
    /**
     * Get the key of a specific index. Returns null if this map doesn't have a key at the specified index.
     *
     * @param index the index
     * @return the value of the index, null if the index can't be found.
     */
    public K getKey(int index) {
        Map.Entry<K, V> entry = this.getEntry(index);
        if (entry == null) {
            return null;
        }
        return entry.getKey();
    }
    /**
     * Get the value of a specific index. Returns null if this map doesn't have a value at the specified index.
     *
     * @param index the index
     * @return the value of the index, null if the index can't be found.
     */
    public V getValue(int index) {
        Map.Entry<K, V> entry = this.getEntry(index);
        if (entry == null) {
            return null;
        }
        return entry.getValue();
    }

    /**
     * Get the entry of a specific index. Returns null if this map doesn't have an entry at the specified index.
     *
     * @param index the index
     * @return the entry of the index, null if the index can't be found.
     */
    public Map.Entry<K, V> getEntry(int index) {
        Set<Map.Entry<K, V>> entries = this.entrySet();
        int j = 0;
        for (Map.Entry<K, V> entry : entries) {
            if (j++ == index) {
                return entry;
            }
        }
        return null;
    }
}
