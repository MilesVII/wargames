package com.milesseventh.wargames;

import java.util.ArrayList;

import com.milesseventh.wargames.Heartstrings.SpecialTechnology;

public class Missile {
	public static final float WEIGHT = 2000;
	
	private float[] tech;
	private ArrayList<SpecialTechnology> st;
	
	@SuppressWarnings("unchecked")
	public Missile(float[] nt, ArrayList<SpecialTechnology> nst) {
		tech = nt.clone();
		st = (ArrayList<SpecialTechnology>)nst.clone();
	}
}
