package com.milesseventh.wargames;

public class Unit {
	public enum Type {FIGHTER, TRANSPORTER, BUILDER};
	
	public Fraction owner;
	public Type type;
	public Structure city;
	
	public Unit(Structure _city, Type _type) {
		city = _city;
		owner = city.ownerFraction;
		type = _type;
	}
}
