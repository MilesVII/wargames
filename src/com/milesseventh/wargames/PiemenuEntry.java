package com.milesseventh.wargames;

public class PiemenuEntry {
	public static final PiemenuEntry PME_CANCEL = new PiemenuEntry("Cancel", null);
	
	public String caption;
	public Callback action;
	
	public PiemenuEntry(String ncaption, Callback naction) {
		caption = ncaption;
		action = naction;
	}

}
