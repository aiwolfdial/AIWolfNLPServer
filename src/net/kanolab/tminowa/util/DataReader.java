package net.kanolab.tminowa.util;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * ファイル読み込み用汎用クラス
 * DataFileReader使用推奨
 */
@Deprecated
public class DataReader {


	private DataFileReader reader;

	public DataReader(String path){
		reader = new DataFileReader(path);
	}

	public DataReader(File file){
		reader = new DataFileReader(file.getAbsolutePath());
	}

	@Deprecated
	public DataReader(String path, String split){
		reader = new DataFileReader(path, split);
	}

	public DataReader(File file, String split){
		reader = new DataFileReader(file.getAbsolutePath(), split);
	}

	/**
	 * このインスタンスが持つファイルの分割済み文字列のリストのコピーを返す。
	 * リストが空ならばread()を呼んで読み込みを行った後にリストを返す。
	 * @return
	 */
	public List<String[]> getSplitLines() {
		return reader.getSplitedLines();
	}

	public List<String> getNonSplitLines(){
		return reader.getLines();
	}

	/**
	 * このインスタンスが持つパス名のファイルを読み込み各行を「,」で分割し、自身を返す
	 * @throws IOException
	 */
	public DataReader read(){
		return this;
	}
}
