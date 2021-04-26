package com.ormoyo.ormoyoutil.util;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class ListDoubleKeyMap<K1, K2, V> implements DoubleKeyMap<K1, K2, V> {
	private final List<DoubleKeyEntry<K1, K2, V>> entries = Lists.newArrayList();
	
	@Override
	public void put(K1 key1, K2 key2, V value) {
		DoubleKeyEntry<K1, K2, V> entry = new MultiKeyListEntry<>(key1, key2, value);
		int i = this.entries.indexOf(entry);
		if(i > -1) {
			this.entries.remove(i);
			this.entries.add(i, entry);
		}else {
			this.entries.add(new MultiKeyListEntry<>(key1, key2, value));
		}
	}
	
	@Override
	public V get(K1 key1, K2 key2) {
		int i = this.containKeys(key1, key2);
		if(i == -1) return null;
		return this.entries.get(i).getValue();
	}
	
	@Override
	public V getOrDefault(K1 key1, K2 key2, V defaultValue) {
		V v = this.get(key1, key2);
		if(v != null) return v;
		return defaultValue;
	}
	
	@Override
	public boolean containsKey1(K1 key) {
		for(DoubleKeyEntry<K1, K2, V> entry : this.entries) {
			K1 k = entry.getKey1();
			if(k == key) return true;
			if(k != null) return k.equals(key);
		}
		return false;
	}
	
	@Override
	public boolean containsKey2(K2 key) {
		for(DoubleKeyEntry<K1, K2, V> entry : this.entries) {
			K2 k = entry.getKey2();
			if(k == key) return true;
			if(k != null) return k.equals(key);
		}
		return false;
	}
	
	@Override
	public boolean containsKeys(K1 key1, K2 key2) {
		return this.containKeys(key1, key2) > -1;
	}
	
	private int containKeys(K1 key1, K2 key2) {
		for(int i = 0; i < this.entries.size(); i++) {
			DoubleKeyEntry<K1, K2, V> entry = this.entries.get(i);
			boolean key1equal = false;
			boolean key2equal = false;
			if(entry.getKey1() == key1) key1equal = true;
			if(entry.getKey1() != null && !key1equal) {
				key1equal = entry.getKey1().equals(key1);
			}
			if(entry.getKey2() == key2) key2equal = true;
			if(entry.getKey2() != null && !key2equal) {
				key2equal = entry.getKey2().equals(key2);
			}
			if(key1equal && key2equal) return i;
		}
		return -1;
	}
	
	@Override
	public Set<DoubleKeyEntry<K1, K2, V>> entrySet() {
		Set<DoubleKeyEntry<K1, K2, V>> set = Sets.newHashSet();
		set.addAll(this.entries);
		return set;
	}
	
	static class MultiKeyListEntry<K1, K2, V> implements DoubleKeyMap.DoubleKeyEntry<K1, K2, V> {
		private V value;
		private K1 key1;
		private K2 key2;
		
		public MultiKeyListEntry(K1 key1, K2 key2, V value) {
			this.key1 = key1;
			this.key2 = key2;
			this.value = value;
		}
		
		public V getValue() {
			return this.value;
		}
		
		public K1 getKey1() {
			return this.key1;
		}
		
		public K2 getKey2() {
			return this.key2;
		}
		
		@Override
		public boolean equals(Object obj) {
			if(this == obj) return true;
			if(obj instanceof DoubleKeyEntry) {
				DoubleKeyEntry<?, ?, ?> entry = (DoubleKeyEntry<?, ?, ?>) obj;
				boolean equal1key = false;
				boolean equal2key = false;
				if(this.getKey1() == entry.getKey1() && this.getKey2() == entry.getKey2()) return true;
				if(this.getKey1() != null) {
					equal1key = this.getKey1().equals(entry.getKey1());
				}
				if(this.getKey2() != null) {
					equal2key = this.getKey2().equals(entry.getKey2());
				}
				return equal1key && equal2key;
			}
			return false;
		}
	}

	@Override
	public V remove(K1 key1, K2 key2) {
		int i = this.containKeys(key1, key2);
		if(i == -1) return null;
		V v = this.entries.get(i).getValue();
		this.entries.remove(i);
		return v;
	}

	@Override
	public boolean remove(K1 key1, K2 key2, V value) {
		int i = this.containKeys(key1, key2);
		if(i == -1) return false;
		V v = this.entries.get(i).getValue();
		if(v == value) {
			this.entries.remove(i);
			return true;
		}
		if(v != null && v.equals(value)) {
			this.entries.remove(i);
			return true;
		}
		return false;
	}

	@Override
	public boolean containsValue(V value) {
		for(DoubleKeyEntry<K1, K2, V> entry : this.entries) {
			if(entry.getValue() == value) return true;
			if(entry.getValue() != null && entry.getValue().equals(value)) return true;
		}
		return false;
	}

	@Override
	public int size() {
		return this.entries.size();
	}
	
    public String toString() {
        Iterator<DoubleKeyEntry<K1, K2, V>> i = entrySet().iterator();
        if (!i.hasNext())
            return "{}";
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        while(true) {
            DoubleKeyEntry<K1, K2, V> e = i.next();
            K1 key1 = e.getKey1();
            K2 key2 = e.getKey2();
            V value = e.getValue();
            sb.append(key1 == this ? "(this Map)" : key1);
            sb.append(" & ");
            sb.append(key2 == this ? "(this Map)" : key2);
            sb.append('=');
            sb.append(value == this ? "(this Map)" : value);
            if (!i.hasNext())
                return sb.append('}').toString();
            sb.append(',').append(' ');
        }
    }
}
