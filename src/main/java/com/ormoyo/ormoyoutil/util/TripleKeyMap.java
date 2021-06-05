package com.ormoyo.ormoyoutil.util;

import java.util.Collection;
import java.util.Set;

public interface TripleKeyMap<K1, K2, K3, V> {
	void put(K1 key1, K2 key2, K3 key3, V value);
	V get(K1 key1, K2 key2, K3 key3);
	V getOrDefault(K1 key1, K2 key2, K3 key3, V defaultValue);
	boolean containsKey1(K1 key);
	boolean containsKey2(K2 key);
	boolean containsKey3(K3 key);
	boolean containsKeys(K1 key1, K2 key2, K3 key3);
	boolean containsValue(V value);
	V remove(K1 key1, K2 key2, K3 key3);
	boolean remove(K1 key1, K2 key2, K3 key3, V value);
	int size();
	void clear();
	Collection<V> values();
	Set<TripleKeyEntry<K1, K2, K3, V>> entrySet();
	public static interface TripleKeyEntry<K1, K2, K3, V> {
		V getValue();
		K1 getKey1();
		K2 getKey2();
		K3 getKey3();
		V setValue(V value);
	}
}
