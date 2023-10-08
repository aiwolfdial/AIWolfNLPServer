package net.kanolab.tminowa.util;

import java.io.PrintStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Debugger {
	private static final boolean DEFAULT_ACTIVATION = false;
	private static final List<Method> OBJECT_METHOD_LIST = Arrays.stream(Object.class.getMethods()).collect(Collectors.toList());

	protected boolean isActive;

	public Debugger(){
		this(DEFAULT_ACTIVATION);
	}

	public Debugger(boolean isActive){
		this.isActive = isActive;
	}


	private String buildPrintText(String methodName, Object object){
		return methodName + " : " + object;
	}

	public boolean isActive(){
		return isActive;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void outputMultiParam(Object object, String methodName, PrintStream ps, int recursionNum){
		String tab = String.join("", IntStream.rangeClosed(0, recursionNum).mapToObj(i -> "\t").collect(Collectors.toList()));
		if(object.getClass().isArray()){
			int length = Array.getLength(object);
			for(int i = 0; i < length; i++)
				println(tab + Array.get(object, i), ps);
			if(length == 0)
				println(tab + "EMPTY", ps);

		}else if(object instanceof Map){
			((Map) object).entrySet().forEach(entry -> println(tab + entry, ps));
			if(((Map)object).size() == 0)
				println(tab + "EMPTY", ps);
		}else if(object instanceof Collection){
			((Collection) object).stream().forEach(o -> println(tab + o, ps));
			if(((Collection)object).size() == 0)
				println(tab + "EMPTY", ps);
		}
	}

	public void print(Object o){
		print(o, System.out);
	}

	public void print(Object o, PrintStream ps){
		print(o, ps::print);
	}

	private <T> void print(T t, Consumer<T> consumer){
		if(!isActive)
			return;
		consumer.accept(t);
	}

	public void println(){
		println(System.out);
	}

	public void println(Object o){
		println(o, System.out);
	}

	public void println(Object o, PrintStream ps){
		print(o, ps::println);
	}

	public void println(PrintStream ps){
		if(!isActive)
			return;
		ps.println();
	}

	public void printMethods(Object object){
		printMethods(object, System.out);
	}

	public void printMethods(Object object, PrintStream ps){
		if(!isActive)
			return;
		ps.println("BEGIN---------------------------------------------------");
//		Arrays.stream(object.getClass().getMethods()).filter(m -> !m.getReturnType().equals(Void.TYPE) && m.getParameterCount() == 0 &&  m.canAccess(object))
		Arrays.stream(object.getClass().getMethods()).filter(m -> !m.getReturnType().equals(Void.TYPE) && m.getParameterCount() == 0)
			.sorted((m1, m2) -> m1.getName().compareTo(m2.getName())).forEach(method ->{
			try {
				Object returnParam = method.invoke(object);
				if(returnParam == null)
					return;

				if(returnParam != null && (returnParam.getClass().isArray() || returnParam instanceof Collection || returnParam instanceof Map)){
					println(buildPrintText(method.getName(), method.getGenericReturnType()), ps);
					outputMultiParam(returnParam, method.getName(), ps, 0);
				}else
					println(buildPrintText(method.getName(), returnParam), ps);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
		});
		ps.println("END---------------------------------------------------");
	}
}
