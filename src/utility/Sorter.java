package utility;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Sorter<V, S> implements Map<V, S>, Serializable {
	@Serial
	private static final long serialVersionUID = -8767639922201898076L;

	private final Map<V, S> itemMap;
	private Comparator<S> scoreComparator;

	public Sorter() {
		this.itemMap = new LinkedHashMap<>();
	}

	public Sorter(Comparator<S> comparator) {
		this();
		this.scoreComparator = comparator;
	}

	public Sorter(Map<? extends V, ? extends S> map) {
		this.itemMap = new LinkedHashMap<>(map);
	}

	public void set(V v, S s) {
		this.itemMap.put(v, s);
	}

	@Override
	public S get(Object v) {
		return this.itemMap.get(v);
	}

	public List<V> getSortedList() {
		return getSortedList(false);
	}

	public List<V> getReverseList() {
		return getSortedList(true);
	}

	private List<V> getSortedList(boolean reverse) {
		List<V> list = new ArrayList<>(this.itemMap.keySet());
		list.sort(new ItemComparator());
		if (reverse) {
			Collections.reverse(list);
		}
		return list;
	}

	private class ItemComparator implements Comparator<V> {
		@Override
		public int compare(V v1, V v2) {
			S s1 = itemMap.get(v1);
			S s2 = itemMap.get(v2);
			if (scoreComparator != null) {
				return scoreComparator.compare(s1, s2);
			} else if (s1 instanceof Comparable<?>) {
				@SuppressWarnings("unchecked")
				Comparable<Object> c1 = (Comparable<Object>) s1;
				return c1.compareTo(s2);
			}
			return 0;
		}
	}

	@Override
	public void clear() {
		this.itemMap.clear();
	}

	@Override
	public boolean containsKey(Object key) {
		return this.itemMap.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return this.itemMap.containsValue(value);
	}

	@Override
	public boolean isEmpty() {
		return this.itemMap.isEmpty();
	}

	@Override
	public Set<V> keySet() {
		return new LinkedHashSet<>(getSortedList());
	}

	@Override
	public S remove(Object key) {
		return this.itemMap.remove(key);
	}

	@Override
	public int size() {
		return this.itemMap.size();
	}

	@Override
	public Collection<S> values() {
		return this.itemMap.values();
	}

	@Override
	public S put(V key, S value) {
		return this.itemMap.put(key, value);
	}

	@Override
	public void putAll(Map<? extends V, ? extends S> m) {
		this.itemMap.putAll(m);
	}

	@Override
	public Set<Map.Entry<V, S>> entrySet() {
		return this.itemMap.entrySet();
	}
}
