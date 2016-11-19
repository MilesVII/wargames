package com.milesseventh.wargames.units;

import com.badlogic.gdx.math.Vector2;
import com.milesseventh.wargames.Territory;

public abstract class IndustrialUnit extends Unit {
	protected float MAX_IP = 1;
	protected int MAX_LEVEL = 2;
	protected int level = 1;
	
	protected IndustrialUnit(Vector2 _pos, float _range, Territory _owner, float _mH) {
		super(_pos, _range, _owner, _mH);
	}
	
	public void levelUp(){
		if (level < MAX_LEVEL)
			level++;
	}
}
