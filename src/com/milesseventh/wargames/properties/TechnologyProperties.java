package com.milesseventh.wargames.properties;

public class TechnologyProperties {
	public int maxMarkup;
	public String title, shortTitle;
	
	public TechnologyProperties(String ntitle, String nshortTitle, int nmaxMarkup) {
		title = ntitle;
		shortTitle = nshortTitle;
		maxMarkup = nmaxMarkup;
	}
}
