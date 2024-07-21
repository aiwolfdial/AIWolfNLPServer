package common.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <div lang="ja">
 *
 * カレンダーに関する静的なメソッドを提供するクラスです。
 *
 * </div>
 *
 * <div lang="en">
 *
 * CalendarTools is that provides a static method on the calendar.
 *
 * </div>
 *
 * @author tori
 *
 */
public class CalendarTools {

	/**
	 * <div lang="ja">
	 *
	 * 指定されたDateオブジェクトを<b>yyyy/MM/dd HH:mm:ss</b>形式の文字列に直して返します。
	 *
	 * </div>
	 *
	 * <div lang="en">
	 *
	 * Converts to <b>yyyy/MM/dd HH:mm:ss</b> format.
	 *
	 * </div>
	 *
	 * @param date
	 *            <div lang="ja">対象とするDateオブジェクト</div>
	 *
	 *            <div lang="en">Date object</div>
	 * @return
	 *
	 *         <div lang="ja">文字列フォーマット</div>
	 *
	 *         <div lang="en">String format</div>
	 */
	public static String toDateTime(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		return sdf.format(date.getTime());
	}

	/**
	 * <div lang="ja">
	 *
	 * 指定されたlong型の値を<b>yyyy/MM/dd HH:mm:ss</b>形式の文字列に直して返します。
	 *
	 * </div>
	 *
	 * <div lang="en">
	 *
	 * Converts to <b>yyyy/MM/dd HH:mm:ss</b> format.
	 *
	 * </div>
	 *
	 * @param time
	 *            <div lang="ja">対象とするlong型の値</div>
	 *
	 *            <div lang="en">Long value</div>
	 * @return
	 *
	 *         <div lang="ja">文字列フォーマット</div>
	 *
	 *         <div lang="en">String format</div>
	 */
	public static String toDateTime(long time) {
		Date date = new Date(time);
		return toDateTime(date);
	}
}
