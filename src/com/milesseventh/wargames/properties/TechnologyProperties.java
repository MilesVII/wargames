package com.milesseventh.wargames.properties;

public class TechnologyProperties {
	public float maxMarkup;
	public String title, shortTitle;
	
	public TechnologyProperties(String _title, String _shortTitle, float _maxMarkup) {
		title = _title;
		shortTitle = _shortTitle;
		maxMarkup = _maxMarkup;
	}
}
