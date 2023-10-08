package net.kanolab.tminowa.util;

public class NumberParser {
	/**
	 * long型をint型へ変換
	 * @param l
	 * @return
	 * @throws NumberFormatException
	 */
	public static Integer parseInt(long l) {
		if(l > Integer.MAX_VALUE) throw new NumberFormatException();
		return Integer.parseInt(String.valueOf(l));
	}
}
