package com.milesseventh.wargames;

import java.util.ArrayList;

import com.badlogic.gdx.math.Vector2;
import com.milesseventh.wargames.units.Mine;
import com.milesseventh.wargames.units.Unit;

public class Territory implements Utils.Marchable{
	private int ownerFraction;
	private ArrayList<Unit> units = new ArrayList <Unit>();
	
	public Territory(int _fractionID){
		ownerFraction = _fractionID;
		build();
	}
	
	public void build(){
		units.add(new Mine(new Vector2(20, 20), 20, this, 0));
		units.add(new Mine(new Vector2(35, 35), 20, this, 0));
	}
	
	public void unregister(Unit _unit){
		units.remove(_unit);
	}

	@Override
	public float getMeta(float x, float y) {
		float _sum = 0;
		for (Unit march: units)
			_sum += Math.pow(march.getOwnRange() /  Vector2.dst(march.getX(), march.getY(), x, y), 5);
		return _sum;
	}

	@Override
	public float getMetaThreshold() {
		return 1;
	}
}
