package net.kanolab.tminowa.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CollectionGenerator {
	public static <T> Set<T> newSet(T... ts) {
		return Arrays.stream(ts).collect(Collectors.toSet());
	}

	public static <T> List<T> newList(T... ts) {
		return Arrays.stream(ts).collect(Collectors.toList());
	}

	@SafeVarargs
	public static <T> Set<T> newSet(Collection<T>... collections) {
		Set<T> set = new HashSet<>();
		for (Collection<T> collection : collections)
			set.addAll(collection);
		return set;
	}
}
