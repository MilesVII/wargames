package com.milesseventh.wargames.units;

import com.badlogic.gdx.math.Vector2;
import com.milesseventh.wargames.Territory;

public abstract class WarUnit extends Unit {
	public WarUnit(Vector2 _pos, float _radius, Territory _owner, float _mH) {
		super(_pos, _radius, _owner, _mH);
	}
}
