package utility;

import java.io.Serial;
import java.util.HashMap;
import java.util.Map;

public class BidiMap<K, V> extends HashMap<K, V> {
	@Serial
	private static final long serialVersionUID = 998600781640480300L;

	private final Map<V, K> inverseMap;

	public BidiMap() {
		this.inverseMap = new HashMap<>();
	}

	public BidiMap(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
		this.inverseMap = new HashMap<>();
	}

	public BidiMap(int initialCapacity) {
		super(initialCapacity);
		this.inverseMap = new HashMap<>();
	}

	public BidiMap(Map<? extends K, ? extends V> m) {
		super(m);
		this.inverseMap = new HashMap<>();
		putAll(m);
	}

	@Override
	public V put(K key, V value) {
		if (containsValue(value)) {
			removeValue(value);
		}
		inverseMap.put(value, key);
		return super.put(key, value);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		for (Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

	public K getKey(V value) {
		return inverseMap.get(value);
	}

	public K removeValue(V value) {
		K key = inverseMap.remove(value);
		if (key != null) {
			super.remove(key);
		}
		return key;
	}

	@Override
	public V remove(Object key) {
		V value = super.remove(key);
		if (value != null) {
			inverseMap.remove(value);
		}
		return value;
	}

	@Override
	public void clear() {
		inverseMap.clear();
		super.clear();
	}
}
