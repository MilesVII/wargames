package com.milesseventh.wargames.units;

import com.badlogic.gdx.math.Vector2;
import com.milesseventh.wargames.Fraction;

public abstract class WarUnit extends Unit {
	public WarUnit(Vector2 _pos, float _radius, float _mH, Fraction _owner) {
		super(_pos, _radius, _mH, _owner);
	}
}
