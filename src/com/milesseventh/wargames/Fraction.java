package com.milesseventh.wargames;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.milesseventh.wargames.units.City;

public class Fraction {
	private int id;
	private String name;
	private Color fractionColor;
	//private ArrayList<Territory> territories = new ArrayList<Territory>();
	private ArrayList<City> cities = new ArrayList<City>();
	private City capital;
	
	public Fraction (int _id, Color _color, String _name, Vector2 _pos){
		id = _id;
		name = _name;
		fractionColor = _color;
		
		capital = new City(_pos, this);
		cities.add(capital);
	}
	
	public void unregisterCity(City _victim){
		cities.remove(_victim);
	}
	
	public void registerCity(City _victim){
		cities.add(_victim);
	}
	
	public ArrayList<City> getCities(){
		return cities;
	}
	
	public Color getColor(){
		return fractionColor;
	}
}
