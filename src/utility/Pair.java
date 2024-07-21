package utility;

import java.io.Serial;
import java.io.Serializable;

public class Pair<K, V> implements Serializable {
	@Serial
	private static final long serialVersionUID = -7137802873518091301L;

	private K key;
	private V value;

	public Pair(K key, V value) {
		this.key = key;
		this.value = value;
	}

	public K getKey() {
		return key;
	}

	public V getValue() {
		return value;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		Pair<?, ?> pair = (Pair<?, ?>) obj;
		return key.equals(pair.key) && value.equals(pair.value);
	}

	@Override
	public int hashCode() {
		return key.hashCode() + value.hashCode();
	}

	@Override
	public String toString() {
		return key + "," + value;
	}
}
