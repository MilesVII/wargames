package com.milesseventh.wargames;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

public class Fraction {
	private int id;
	private String name;
	private Color fractionColor;
	private ArrayList<Structure> structs = new ArrayList<Structure>();
	private Structure capital;
	
	public Fraction (int _id, Color _color, String _name, Vector2 _pos){
		id = _id;
		name = _name;
		fractionColor = _color;
		
		capital = new Structure(_pos, Structure.StructureType.CITY, this);
		structs.add(capital);
	}
	
	public void unregisterStructure(Structure _victim){
		structs.remove(_victim);
	}
	
	public void registerStructure(Structure _victim){
		structs.add(_victim);
	}
	
	public ArrayList<Structure> getStructs(){
		return structs;
	}
	
	public Color getColor(){
		return fractionColor;
	}
	
	public Structure getCapital(){
		return capital;
	}
}
