package com.ormoyo.ormoyoutil.util;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import com.google.common.collect.Lists;
import com.ormoyo.ormoyoutil.util.DoubleKeyMap.DoubleKeyEntry;

public class TripleKeyListMap<K1, K2, K3, V> implements TripleKeyMap<K1, K2, K3, V> {
	private EntrySet entryset;
	private final List<TripleKeyEntry<K1, K2, K3, V>> entries = Lists.newArrayList();
	
	@Override
	public void put(K1 key1, K2 key2, K3 key3, V value) {
		TripleKeyEntry<K1, K2, K3, V> entry = new TripleKeyListEntry<>(key1, key2, key3, value);
		int i = this.entries.indexOf(entry);
		if(i > -1) {
			this.entries.remove(i);
			this.entries.add(i, entry);
		}else {
			this.entries.add(new TripleKeyListEntry<>(key1, key2, key3, value));
		}
	}
	
	@Override
	public V get(K1 key1, K2 key2, K3 key3) {
		int i = this.containKeys(key1, key2, key3);
		if(i == -1) return null;
		return this.entries.get(i).getValue();
	}
	
	@Override
	public V getOrDefault(K1 key1, K2 key2, K3 key3, V defaultValue) {
		V v = this.get(key1, key2, key3);
		return v != null ? v : defaultValue;
	}
	
	@Override
	public boolean containsKey1(K1 key) {
		for(TripleKeyEntry<K1, K2, K3, V> entry : this.entries) {
			K1 k = entry.getKey1();
			if(k == key) return true;
			if(k != null) return k.equals(key);
		}
		return false;
	}
	
	@Override
	public boolean containsKey2(K2 key) {
		for(TripleKeyEntry<K1, K2, K3, V> entry : this.entries) {
			K2 k = entry.getKey2();
			if(k == key) return true;
			if(k != null) return k.equals(key);
		}
		return false;
	}
	
	@Override
	public boolean containsKey3(K3 key) {
		for(TripleKeyEntry<K1, K2, K3, V> entry : this.entries) {
			K3 k = entry.getKey3();
			if(k == key) return true;
			if(k != null) return k.equals(key);
		}
		return false;
	}
	
	@Override
	public boolean containsKeys(K1 key1, K2 key2, K3 key3) {
		return this.containKeys(key1, key2, key3) > -1;
	}
	
	private int containKeys(K1 key1, K2 key2, K3 key3) {
		for(int i = 0; i < this.entries.size(); i++) {
			TripleKeyEntry<K1, K2, K3, V> entry = this.entries.get(i);
			boolean key1equal = false;
			boolean key2equal = false;
			boolean key3equal = false;
			if(entry.getKey1() == key1) key1equal = true;
			if(entry.getKey1() != null && !key1equal) {
				key1equal = entry.getKey1().equals(key1);
			}
			if(entry.getKey2() == key2) key2equal = true;
			if(entry.getKey2() != null && !key2equal) {
				key2equal = entry.getKey2().equals(key2);
			}
			if(entry.getKey3() == key3) key3equal = true;
			if(entry.getKey3() != null && !key3equal) {
				key3equal = entry.getKey3().equals(key3);
			}
			if(key1equal && key2equal && key3equal) return i;
		}
		return -1;
	}
	
	@Override
	public Set<TripleKeyEntry<K1, K2, K3, V>> entrySet() {
        Set<TripleKeyEntry<K1, K2, K3,V>> es;
        return (es = this.entryset) == null ? (this.entryset = new EntrySet()) : es;
	}
	
    final class EntrySet extends AbstractSet<TripleKeyEntry<K1, K2, K3, V>> {
        public final int size()                 { return TripleKeyListMap.this.size(); }
        public final void clear()               { TripleKeyListMap.this.clear(); }
        public final Iterator<TripleKeyEntry<K1, K2, K3, V>> iterator() {
            return new Iterator<TripleKeyEntry<K1, K2, K3, V>>() {
            	boolean hasRemoved;
            	int index = -1;
				@Override
				public boolean hasNext() {
					return this.index + 1 < TripleKeyListMap.this.entries.size();
				}

				@Override
				public TripleKeyEntry<K1, K2, K3, V> next() {
					this.hasRemoved = false;
					this.index++;
					TripleKeyEntry<K1, K2, K3, V> entry = TripleKeyListMap.this.entries.get(this.index);
					return entry;
				}
				
				@Override
				public void remove() {
					if(this.index == -1) throw new IllegalStateException("remove");
					if(this.hasRemoved) throw new IllegalStateException("remove");
					this.hasRemoved = true;
					TripleKeyListMap.this.entries.remove(this.index);
				}
			};
        }
        
        public final boolean contains(Object o) {
            if (!(o instanceof DoubleKeyEntry))
                return false;
            return TripleKeyListMap.this.entries.contains(o);
        }
        
        public final boolean remove(Object o) {
            if (o instanceof DoubleKeyEntry) {
            	TripleKeyListMap.this.entries.remove(o);
            }
            return false;
        }
        public final void forEach(Consumer<? super TripleKeyEntry<K1, K2, K3, V>> action) {
        	for(TripleKeyEntry<K1, K2, K3, V> entry : TripleKeyListMap.this.entries) {
        		action.accept(entry);
        	}
        }
    }
	
	static class TripleKeyListEntry<K1, K2, K3, V> implements TripleKeyMap.TripleKeyEntry<K1, K2, K3, V> {
		private V value;
		private K1 key1;
		private K2 key2;
		private K3 key3;
		
		public TripleKeyListEntry(K1 key1, K2 key2, K3 key3, V value) {
			this.key1 = key1;
			this.key2 = key2;
			this.key3 = key3;
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
		
		public K3 getKey3() {
			return this.key3;
		}
		
		@Override
		public boolean equals(Object obj) {
			if(this == obj) return true;
			if(obj instanceof TripleKeyEntry) {
				TripleKeyEntry<?, ?, ?, ?> entry = (TripleKeyEntry<?, ?, ?, ?>) obj;
				boolean equal1key = false;
				boolean equal2key = false;
				boolean equal3key = false;
				if(this.getKey1() == entry.getKey1() && this.getKey2() == entry.getKey2() && this.getKey3() == entry.getKey3()) return true;
				if(this.getKey1() != null) {
					equal1key = this.getKey1().equals(entry.getKey1());
				}
				if(this.getKey2() != null) {
					equal2key = this.getKey2().equals(entry.getKey2());
				}
				if(this.getKey3() != null) {
					equal3key = this.getKey3().equals(entry.getKey3());
				}
				return equal1key && equal2key && equal3key;
			}
			return false;
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
            sb.append(key1);
            sb.append(" & ");
            sb.append(key2);
            sb.append(" & ");
            sb.append(key3);
            sb.append(" = ");
            sb.append(value);
			return sb.toString();
		}

		@Override
		public V setValue(V value) {
			V v = this.value;
			this.value = value;
			return v;
		}
	}

	@Override
	public V remove(K1 key1, K2 key2, K3 key3) {
		int i = this.containKeys(key1, key2, key3);
		if(i == -1) return null;
		V v = this.entries.get(i).getValue();
		this.entries.remove(i);
		return v;
	}

	@Override
	public boolean remove(K1 key1, K2 key2, K3 key3, V value) {
		int i = this.containKeys(key1, key2, key3);
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
		for(TripleKeyEntry<K1, K2, K3, V> entry : this.entries) {
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
        Iterator<TripleKeyEntry<K1, K2, K3, V>> i = entrySet().iterator();
        if (!i.hasNext())
            return "{}";
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        while(true) {
            TripleKeyEntry<K1, K2, K3, V> e = i.next();
            K1 key1 = e.getKey1();
            K2 key2 = e.getKey2();
            K3 key3 = e.getKey3();
            V value = e.getValue();
            sb.append(key1 == this ? "(this Map)" : key1);
            sb.append(" & ");
            sb.append(key2 == this ? "(this Map)" : key2);
            sb.append(" & ");
            sb.append(key3 == this ? "(this Map)" : key3);
            sb.append(" = ");
            sb.append(value == this ? "(this Map)" : value);
            if (!i.hasNext())
                return sb.append('}').toString();
            sb.append(',').append(' ');
        }
    }

	@Override
	public Collection<V> values() {
		List<V> v = Lists.newArrayList();
		this.entries.forEach(e -> v.add(e.getValue()));
		return v;
	}

	@Override
	public void clear() {
		this.entries.clear();
	}
}
