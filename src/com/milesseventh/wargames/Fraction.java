package com.milesseventh.wargames;

import java.util.ArrayList;

public class Fraction {
	private int id;
	private String name;
	private ArrayList<Territory> territories = new ArrayList<Territory>();
	
	public Fraction (int _id, String _name, float _x, float _y){
		id = _id;
		name = _name;
		//build city at x y
	}
}
