package net.kanolab.tminowa.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CollectionConverter {
	/**
	 * DoubleをValueに持つマップについて、最大値が1, 最少値が0になるように正規化したMapを返す<br>
	 * 正規化後の各Valueは小数点第17位で四捨五入され、丸め誤差により正規化後のvalueの合計値が1でない場合がある<br>
	 * @param map
	 * @return valueの値が0～1になるように正規化されたMap
	 */
	public static <T> Map<T, Double> normalize(Map<T, Double> map){
		final double max = map.values().stream().max(Double::compareTo).get();
		final double min = map.values().stream().min(Double::compareTo).get();
		return map.entrySet().stream().collect(Collectors.toConcurrentMap(Entry::getKey,
				entry -> new BigDecimal((entry.getValue() - min) / (max - min))
				.setScale(17, RoundingMode.HALF_UP).doubleValue()));
	}

	public static <T, U> Map<T, List<U>> removeEmptyValues(Map<T, List<U>> map){
		Map<T, List<U>> m = new HashMap<>(map);
		Iterator<Entry<T, List<U>>> iterator = m.entrySet().iterator();
		while(iterator.hasNext()){
			Entry<T, List<U>> entry = iterator.next();
			if(entry.getValue().isEmpty())
				iterator.remove();
		}
		return m;
	}

	/**
	 * doubleをvalueに持つMapからsortされたkeyのListを返す
	 * @param map
	 * @param sortReverse
	 * @return
	 */
	public static <T> List<T> getSortedList(Map<T, Double> map, boolean sortReverse){
		int i = sortReverse ? -1 : 1;
		return map.entrySet().stream().sorted((e1, e2) -> e1.getValue().compareTo(e2.getValue()) * i)
			.map(Entry::getKey).collect(Collectors.toList());
	}

	/**
	 * 指定したMapについて、comparatorでソートした後funcで変換した結果のリストを返す
	 * @param map
	 * @param comparator
	 * @param func
	 * @return
	 */
	public static <T, U, R> List<R> getSortedList(Map<T, U> map,
		Comparator<Entry<T, U>> comparator, Function<Entry<T, U>, R> func){
		return map.entrySet().stream().sorted(comparator::compare).map(func::apply).collect(Collectors.toList());
	}

	/**
	 * 指定したMapについてcomparatorでソートしたエントリーのリストを返す
	 * @param map
	 * @param comparator
	 * @return
	 */
	public static <T, U, R> List<Entry<T,U>> getSortedList(Map<T, U> map, Comparator<Entry<T, U>> comparator){
		return map.entrySet().stream().sorted(comparator::compare).collect(Collectors.toList());
	}

	/**
	 * Collectionをvalueに持つMapから全てのvalueのCollectionの要素を持つListを返す
	 * @param map
	 * @return
	 */
	public static <T, U> List<T> getValues(Map<U, List<T>> map){
		List<T> list = new ArrayList<>();
		map.values().forEach(list::addAll);
		return list;
	}

	public static <T> List<T> getValues(List<List<T>> lists){
		List<T> list = new ArrayList<>();
		for(List<T> l : lists){
			list.addAll(l);
		}
		return list;
	}

	/**
	 * 正のDoubleをValueに持つMapについて、valueの合計が1になるように正規化したMapを返す<br>
	 * 負数が含まれていた場合の動作は保証されない<br>
	 * @param map
	 * @return
	 * @throws NumberFormatException valueの合計が0になる場合
	 */
	public static <T> Map<T, Double> normalizeRatio(Map<T, Double> map){
		return normalizeRatio(map, 1.0);
	}
	
	/**
	 * 正のDoubleをValueに持つMapについて、valueの合計が1になるように正規化したMapを返す<br>
	 * 負数が含まれていた場合の動作は保証されない<br>
	 * @param map
	 * @return
	 * @throws NumberFormatException valueの合計が0になる場合
	 */
	public static <T> Map<T, Double> normalizeRatio(Map<T, Double> map, double normalizedSum){

		final double sum = Math.abs(map.values().stream().mapToDouble(Double::doubleValue).sum());
		if(sum == 0) {
			return map.entrySet().stream().collect(Collectors.toConcurrentMap(Entry::getKey,
					entry -> new BigDecimal(0)
					.setScale(17, RoundingMode.HALF_UP).doubleValue()));
		}
		if(sum == 0) {
			return map.entrySet().stream().collect(Collectors.toConcurrentMap(Entry::getKey,
					entry -> new BigDecimal(0)
					.setScale(17, RoundingMode.HALF_UP).doubleValue()));
		}
		return map.entrySet().stream().collect(Collectors.toConcurrentMap(Entry::getKey,
				entry -> new BigDecimal(entry.getValue() / sum * normalizedSum)
				.setScale(17, RoundingMode.HALF_UP).doubleValue()));
	}
	
	public static <T> Map<T, Double> softMax(Map<T, Double> map, Double t) {
		Double maxValue = map.values().stream().mapToDouble(Double::doubleValue).max().getAsDouble();
		Map<T, Double> values = map.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey(),  entry -> Math.exp((entry.getValue() - maxValue)/t)));
		Double total = values.values().stream().mapToDouble(x -> x).sum();
		return values.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey(),  entry -> entry.getValue() / total));
	}

	/**
	 * valueが0から1に正規化されたMapについて、valueを反転させたMapを返す<br>
	 * この操作は各valueについて1 - valueの絶対値を取ったMapに等しい<br>
	 * @param ratioMap
	 * @return 確率値を反転させたMap
	 */
	public static <T> Map<T, Double> reverseRatio(Map<T, Double> ratioMap){
		return ratioMap.entrySet().stream()
			.collect(Collectors.toConcurrentMap(Entry::getKey, entry -> Math.abs(1 - entry.getValue())));
	}

	/**
	 * 2つのMapを指定の方法によって統合したMapを返す<br>
	 * m1, m2間のkeyが完全に一致していない場合の動作は保証されない
	 * @param mergeFunc
	 * @param maps
	 * @return mergeFuncによってValueを統合したMap
	 */
	public static <T, U> Map<T, U> merge(BiFunction<U, U, U> mergeFunc,
			Map<T, U> m1, Map<T, U> m2){
		return m1.keySet().stream()
			.collect(Collectors.toConcurrentMap(key -> key, key -> {
				return mergeFunc.apply(m1.get(key), m2.get(key));
		}));
	}

	/**
	 * double型の値を持つ2つのMapを統合したMapを返す<br>
	 * m1, m2間のkeyが完全に一致していない場合の動作は保証されない<br>
	 * Valueは幾何平均を取る<br>
	 * @param <T>
	 * @param maps
	 * @return valueの幾何平均を取ったMap
	 */
	public static <T> Map<T, Double> merge(Map<T, Double> m1, Map<T, Double> m2){
		return merge((d1, d2) -> Math.sqrt(d1 * d2), m1, m2);
	}

	/**
	 * 任意の数のMapを指定の方法によって統合したMapを返す<br>
	 * 各map間のkeyが完全に一致していない場合の動作は保証されない<br>
	 * @param mergeFunc
	 * @param maps
	 * @return mergeFuncによってvalueを統合したMap
	 */
	public static <T, U> Map<T, U> merge(Function<Collection<U>, U> mergeFunc,
			List<Map<T, U>> maps){
		return maps.get(0).keySet().stream().collect(Collectors.toConcurrentMap(key -> key, key ->{
			return mergeFunc.apply(maps.stream().map(map -> map.get(key)).collect(Collectors.toSet()));
		}));
	}

	/**
	 * double型のvalueを持つ任意の数のMapを幾何平均によって統合したMapを返す<br>
	 * 各map間のkeyが完全に一致していない場合の動作は保証されない<br>
	 * @param maps
	 * @return 幾何平均によってvalueを統合したMap
	 */
	public static <T> Map<T, Double> merge(List<Map<T, Double>> maps){
		return maps.get(0).keySet().stream().collect(Collectors.toConcurrentMap(key -> key, key ->{
			return NumberCalculator.getGeometricMean(maps.stream().map(map -> map.get(key)).collect(Collectors.toList()));
		}));
	}

	/**
	 * コレクション要素をKey, 出現頻度をValueに取るMapを返す
	 * @param collection
	 * @return 頻度マップ
	 */
	public static <T> Map<T, Integer> getFrequencyMap(Collection<T> collection){
		return getFrequencyMap(collection, t -> t);
	}

	/**
	 * コレクション要素を指定の方法で変換した要素をKey, 出現頻度をValueに取るMapを返す
	 * @param collection
	 * @return 頻度マップ
	 */
	public static <T, U> Map<U, Integer> getFrequencyMap(Collection<T> collection, Function<T, U> func){
		return collection.stream().map(func::apply).collect(Collectors.groupingBy(t -> t, Collectors.counting()))
				.entrySet().stream().map(entry -> new SimpleEntry<U, Integer>(entry.getKey(), NumberParser.parseInt(entry.getValue())))
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	}

	/**
	 * 最頻出の要素のみを含むリストを返す<br>
	 * @param collection
	 * @return 最頻出要素
	 */
	public static <T> List<T> getMostFrequencyList(Collection<T> collection){
		return getMostFrequencyList(collection, t -> t);
	}

	/**
	 * コレクション要素を指定の方法で変換した後、最頻出の要素のみを含むリストを返す<br>
	 * @param collection
	 * @param func
	 * @return
	 */
	public static <T, U> List<U> getMostFrequencyList(Collection<T> collection, Function<T, U> func){
		List<U> list = new ArrayList<>();
		int frequency = 0;
		for(Entry<U, Integer> entry : getFrequencyMap(collection, func).entrySet()) {
			if(entry.getValue() < frequency)
				continue;
			if(entry.getValue() > frequency)
				list.clear();
			list.add(entry.getKey());
			frequency = entry.getValue();
		}
		return list;
	}
}
