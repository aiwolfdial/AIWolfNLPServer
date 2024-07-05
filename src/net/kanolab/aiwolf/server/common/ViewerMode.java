package net.kanolab.aiwolf.server.common;

public enum ViewerMode {
	CUI,
	GUI,
	;

	public static ViewerMode stringToParam(String s) {
		for (ViewerMode v : ViewerMode.values()) {
			System.out.println("viewerMode -> " + v.toString() + " : " + s);
			if (v.toString().equalsIgnoreCase(s))
				return v;
		}
		return null;
	}
}
