package com.milesseventh.wargames.units;

import com.badlogic.gdx.math.Vector2;
import com.milesseventh.wargames.Territory;

public class Mine extends Unit {
	private City owner;
	
	public Mine(Vector2 _pos, float _range, float _mH, City _owner) {
		super(_pos, _range, _mH, _owner.getFraction());
		_owner.addMine(this);
		owner = _owner;
	}
	
	@Override
	public void unregister(){
		owner.mineDestroyed(this);
	}
	
	/*
	private float getMiningPower(int _level){
		return MAX_MP * (level / MAX_LEVEL);
	}
	
	public void levelUp(){
		if (level < MAX_LEVEL)
			level++;
	}
	
	public int getLevel(){
		return level;
	}
	
	public float getMP(){
		return getMiningPower(level);
	}*/
}