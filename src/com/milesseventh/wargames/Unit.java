package com.milesseventh.wargames;

import com.badlogic.gdx.math.MathUtils;
import com.milesseventh.wargames.Heartstrings.Technology;

public class Unit {
	public enum Type {FIGHTER, TRANSPORTER, BUILDER};
	
	public static final float MAX_CARGO = 1;
	
	public float[] techLevel = {0, 0, 0, 0, 0, 0};
	public Faction owner;
	public Type type;
	public Structure manufacturer;
	
	public Unit(Structure _manufacturer, Type _type) {
		manufacturer = _manufacturer;
		owner = manufacturer.ownerFaction;
		type = _type;
	}
	
	public void setTechLevel(Technology t, float in){
		techLevel[t.ordinal()] = MathUtils.clamp(in, 0, 1);
	}
	
	@Override
	public int hashCode(){
		int h = 17;
		for (float r: techLevel)
			h = h * 71 + (int)(r * 100);
		h = h * 71 + manufacturer.hashCode();
		h = h * 71 + type.ordinal();
		return h;
	}
	
	@Override
	public boolean equals(Object o){
		if (o == this)
			return true;
		if (!(o instanceof Unit))
			return false;
		Unit u = (Unit) o;
		
		for (int i = 0; i < techLevel.length; i++)
			if (techLevel[i] != u.techLevel[i])
				return false;
		return (type == u.type);
	}
	
	
}
