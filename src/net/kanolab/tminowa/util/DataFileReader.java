package net.kanolab.tminowa.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataFileReader {
	private final static String DEFAULT_COMMENTOUT_WORD = "#";
	private final static String DEFAULT_SPLIT_WORD = ",";
	private final static boolean DEFAULT_IGNORE_EMPTY_LINE = true;

	private String commentoutWord;
	private String splitWord;
	private Path path;

	boolean ignoreEmptyLine;

	public DataFileReader(Path path){
		this(path, DEFAULT_COMMENTOUT_WORD, DEFAULT_SPLIT_WORD);
	}

	public DataFileReader(Path path, boolean ignoreEmptyLine){
		this(path, DEFAULT_COMMENTOUT_WORD, DEFAULT_SPLIT_WORD, ignoreEmptyLine);
	}

	public DataFileReader(Path path, String splitWord){
		this(path, DEFAULT_COMMENTOUT_WORD, splitWord);
	}

	public DataFileReader(Path path, String splitWord, boolean ignoreEmptyLine){
		this(path, DEFAULT_COMMENTOUT_WORD, splitWord, ignoreEmptyLine);
	}

	public DataFileReader(Path path, String commentoutWord, String splitWord){
		this(path, commentoutWord, splitWord, DEFAULT_IGNORE_EMPTY_LINE);
	}

	public DataFileReader(Path path, String commentoutWord, String splitWord, boolean ignoreEmptyLine){
		this.path = path;
		this.commentoutWord = commentoutWord;
		this.splitWord = splitWord;
		this.ignoreEmptyLine = ignoreEmptyLine;
	}

	public DataFileReader(String path){
		this(Paths.get(path));
	}

	public DataFileReader(String path, boolean ignoreEmptyLine){
		this(Paths.get(path), ignoreEmptyLine);
	}

	public DataFileReader(String path, String splitWord){
		this(Paths.get(path), DEFAULT_COMMENTOUT_WORD, splitWord);
	}

	public DataFileReader(String path, String splitWord, boolean ignoreEmptyLine){
		this(Paths.get(path), DEFAULT_COMMENTOUT_WORD, splitWord, ignoreEmptyLine);
	}

	/**
	 * コメントアウトされた行以外のすべての行を要素に持つListを返す
	 * @return コメントアウトされていない行のList
	 */
	public List<String> getLines(){
		return getLineStream().collect(Collectors.toList());
	}

	/**
	 * コメントアウトされた行以外のすべての行を要素に持つStreamを返す
	 * @return コメントアウトされていない行のStream
	 */
	public Stream<String> getLineStream(){
		return read(Function.identity());
	}

	/**
	 * コメントアウトされた行以外のすべての行をsplitした配列を要素に持つListを返す
	 * @return コメントアウトされていない行をsplitした配列のList
	 */
	public List<String[]> getSplitedLines(){
		return getSplitedLineStream().collect(Collectors.toList());
	}

	/**
	 * コメントアウトされた行以外のすべての行をsplitした配列を要素に持つStreamを返す
	 * @return コメントアウトされていない行をsplitした配列のStream
	 */
	public Stream<String[]> getSplitedLineStream(){
		return read(s -> s.split(splitWord));
	}

	/**
	 * ファイルを読み込み、コメントアウトする行を除外し、指定の方法で変換した要素を持つStreamを返す
	 * @param func
	 * @return ファイルの変換後Stream
	 */
	private <T> Stream<T> read(Function<String, T> func){
		try {
			return Files.lines(path).filter(s -> !s.startsWith(commentoutWord) && !(ignoreEmptyLine && s.isEmpty())).map(func::apply);
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		return null;
	}
}
