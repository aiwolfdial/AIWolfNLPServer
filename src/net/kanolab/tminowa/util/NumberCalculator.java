package net.kanolab.tminowa.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;

/**
 * 計算、コレクションの変換に関する汎用クラス
 */
public class NumberCalculator{
	/**
	 * 幾何平均を返す
	 * @param doubles
	 * @return
	 */
	public static double getGeometricMean(Collection<Double> doubles) {
		double average = 1;
		for(double d : doubles) {
			average *= d;
		}
		return new BigDecimal(Math.pow(average, 1 / (double)doubles.size())).setScale(17, RoundingMode.HALF_UP).doubleValue();
	}
}
