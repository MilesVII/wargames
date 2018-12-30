package com.milesseventh.wargames;

import java.util.ArrayList;

import com.milesseventh.wargames.Heartstrings.SpecialTechnology;

public class Missile {
	public static final float WEIGHT = Utils.remap(.5f, 0, 1, Unit.MIN_CARGO, Unit.MAX_CARGO);
	
	private float[] tech;
	private ArrayList<SpecialTechnology> st;
	
	@SuppressWarnings("unchecked")
	public Missile(float[] nt, ArrayList<SpecialTechnology> nst) {
		tech = nt.clone();
		st = (ArrayList<SpecialTechnology>)nst.clone();
	}
}
