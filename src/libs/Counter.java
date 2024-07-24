package libs;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Counter<V> implements Iterable<V>, Map<V, Integer> {
	private final Sorter<V, Integer> sorter;

	public Counter() {
		sorter = new Sorter<>();
	}

	public void add(V v, int num) {
		sorter.put(v, sorter.getOrDefault(v, 0) + num);
	}

	public void add(V v) {
		add(v, 1);
	}

	public Integer get(Object v) {
		return sorter.getOrDefault(v, 0);
	}

	public V getLargest() {
		return sorter.isEmpty() ? null : sorter.getReverseList().getFirst();
	}

	public void clear() {
		sorter.clear();
	}

	public boolean containsKey(Object key) {
		return sorter.containsKey(key);
	}

	public boolean isEmpty() {
		return sorter.isEmpty();
	}

	public Set<V> keySet() {
		return sorter.keySet();
	}

	public Integer remove(Object key) {
		return sorter.remove(key);
	}

	public int size() {
		return sorter.size();
	}

	public Integer put(V key, Integer value) {
		return sorter.put(key, value);
	}

	public void putAll(Map<? extends V, ? extends Integer> m) {
		sorter.putAll(m);
	}

	@Override
	public boolean containsValue(Object value) {
		return sorter.containsValue(value);
	}

	@Override
	public Collection<Integer> values() {
		return sorter.values();
	}

	@Override
	public Set<Map.Entry<V, Integer>> entrySet() {
		return sorter.entrySet();
	}

	@Override
	public Iterator<V> iterator() {
		return sorter.getSortedList().iterator();
	}
}
