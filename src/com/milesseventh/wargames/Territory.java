package com.milesseventh.wargames;

import java.util.ArrayList;

import com.milesseventh.wargames.units.Unit;

public class Territory {
	private int ownerFraction;
	private ArrayList<Unit> units = new ArrayList <Unit>();
	
	public Territory(int _fractionID){
		ownerFraction = _fractionID;
	}
	
	public void build(){
		//units.add(new Mine(ownerFraction, 100));
	}
	
	public void unregister(Unit _unit){
		units.remove(_unit);
	}
}
