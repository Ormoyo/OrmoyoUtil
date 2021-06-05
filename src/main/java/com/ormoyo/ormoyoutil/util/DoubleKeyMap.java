package com.ormoyo.ormoyoutil.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface DoubleKeyMap<K1, K2, V> {
	void put(K1 key1, K2 key2, V value);
	V get(K1 key1, K2 key2);
	V getOrDefault(K1 key1, K2 key2, V defaultValue);
	boolean containsKey1(K1 key);
	boolean containsKey2(K2 key);
	boolean containsKeys(K1 key1, K2 key2);
	boolean containsValue(V value);
	V remove(K1 key1, K2 key2);
	boolean remove(K1 key1, K2 key2, V value);
	int size();
	void clear();
	Collection<V> values();
	Set<DoubleKeyEntry<K1, K2, V>> entrySet();
	public static interface DoubleKeyEntry<K1, K2, V> {
		V getValue();
		K1 getKey1();
		K2 getKey2();
		V setValue(V value);
	}
}
