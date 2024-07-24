package libs;

import java.io.Serial;
import java.io.Serializable;

public record Pair<K, V>(K key, V value) implements Serializable {
	@Serial
	private static final long serialVersionUID = -7137802873518091301L;

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
	public String toString() {
		return String.format("%s,%s", key, value);
	}
}
