package com.milesseventh.wargames.units;

import com.badlogic.gdx.math.Vector2;
import com.milesseventh.wargames.Territory;

public class Mine extends MiningUnit {
	private static final int MAX_LEVEL = 3, MAX_MP = 7;
	private int level = 1;
	
	public Mine(Vector2 _pos, float _range, Territory _owner, float _mH) {
		super(_pos, _range, _owner, _mH);
	}
	
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
	}
}
