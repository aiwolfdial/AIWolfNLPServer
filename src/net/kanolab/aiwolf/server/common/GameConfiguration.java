package net.kanolab.aiwolf.server.common;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.aiwolf.common.data.Role;

import net.kanolab.tminowa.util.DataReader;
import net.kanolab.tminowa.util.Debugger;

/**
 * ゲームの設定
 * 
 * @author tminowa
 *
 */
public class GameConfiguration implements Cloneable {
	// iniファイルにてオプション名と値を分ける記号
	private static final String OPTION_PARAM_SEPARATOR = "=";
	private Debugger debugger = new Debugger();

	/**
	 * オプションを格納するMap
	 */
	private Map<Class<?>, Map<Option, ?>> optionMap = new HashMap<>();

	/**
	 * optionMapへデータの追加
	 * 
	 * @param option
	 * @param paramString
	 */
	private void putOptionParam(Option option, String paramString) {
		Object paramObject = null;
		Class<?> c = option.getParamClass();

		Map map;
		if (optionMap.containsKey(c)) {
			map = optionMap.get(c);
		} else {
			map = new HashMap();
			optionMap.put(c, map);
		}

		debugger.println(option + " : " + paramString + " : " + c.getName());
		try {
			// オプションの値に列挙型を使用している場合のみstringToParamメソッドを呼び出す
			if (Arrays.asList(Option.ENUM_OPTIONS).contains(option))
				paramObject = c.getMethod("stringToParam", new Class<?>[] { String.class }).invoke("!", paramString);
			else
				paramObject = c.getConstructor(String.class).newInstance(paramString);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException | InstantiationException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		map.put(option, paramObject);
	}

	public static void main(String[] args) {
		GameConfiguration config = new GameConfiguration("./config/NLPAIWolfServer.ini");
		System.out.println(config.getGameMode());
	}

	/**
	 * 設定ファイルの読み込み
	 * 
	 * @param initFileName
	 */
	public GameConfiguration(String initFileName) {
		DataReader reader = new DataReader(initFileName);
		for (String line : reader.getNonSplitLines()) {
			if (!line.contains(OPTION_PARAM_SEPARATOR))
				continue;

			String[] data = line.split(OPTION_PARAM_SEPARATOR);
			Option option = Option.get(data[0]);

			// 値が不正なら無視
			if (data.length != 2 || option == null) {
				continue;
			}

			// パラメータの保存
			putOptionParam(option, data[1].trim());
		}
		// 追加されていないものに対してデフォルトパラメータを与える
		for (Option option : Option.values()) {
			if (!optionMap.containsKey(option.getParamClass())
					|| !optionMap.get(option.getParamClass()).containsKey(option)) {
				putOptionParam(option, option.getDefaultParam());
			}
		}
	}

	/**
	 * 引数のGameConfigurationのオプション一覧をシャローコピーしたGameConfigurationを生成する
	 * 
	 * @param original
	 */
	public GameConfiguration(GameConfiguration original) {
		this.optionMap = new HashMap<Class<?>, Map<Option, ?>>(original.optionMap);
	}

	/**
	 * 設定の取得
	 * 
	 * @param <T>
	 * @param option
	 * @return
	 */
	public <T> T get(Option option) {
		return (T) optionMap.get(option.getParamClass()).get(option);
	}

	/**
	 * int型で設定を取得する<br>
	 * int型以外の場合はClassCastExceptionを投げる
	 * 
	 * @param option
	 * @return
	 */
	public int getInt(Option option) throws ClassCastException {
		return (int) optionMap.get(option.getParamClass()).get(option);
	}

	/**
	 * long型で設定を取得する<br>
	 * long型以外の場合はClassCastExceptionを投げる
	 * 
	 * @param option
	 * @return
	 */
	public long getLong(Option option) throws ClassCastException {
		return (long) optionMap.get(option.getParamClass()).get(option);
	}

	/**
	 * boolean型で設定を取得する<br>
	 * boolean型以外の場合はClassCastExceptionを投げる
	 * 
	 * @param option
	 * @return
	 */
	public boolean getBoolean(Option option) throws ClassCastException {
		return (boolean) optionMap.get(option.getParamClass()).get(option);
	}

	public ViewerMode getViewerMode() {
		return (ViewerMode) optionMap.get(Option.VIEWER_MODE.getParamClass()).get(Option.VIEWER_MODE);
	}

	public GameMode getGameMode() {
		return (GameMode) optionMap.get(Option.GAME_MODE.getParamClass()).get(Option.GAME_MODE);
	}

	public Role getHumanRole() {
		HumanRole role = (HumanRole) optionMap.get(Option.HUMAN_ROLE.getParamClass()).get(Option.HUMAN_ROLE);
		if (role == HumanRole.NULL)
			return null;
		for (Role r : Role.values()) {
			if (r.toString().equalsIgnoreCase(role.toString()))
				return r;
		}
		return null;
	}

	/**
	 * String型で設定を取得する<br>
	 * toString()メソッドを利用する
	 * 
	 * @param option
	 * @return
	 */
	public String getString(Option option) throws ClassCastException {
		return optionMap.get(option.getParamClass()).get(option).toString();
	}
}
