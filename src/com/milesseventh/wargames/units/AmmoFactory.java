package com.milesseventh.wargames.units;

import com.badlogic.gdx.math.Vector2;
import com.milesseventh.wargames.Territory;

public class AmmoFactory extends ResUnit {
	protected float MAX_IP = 5;
	protected int MAX_LEVEL = 2;
	
	protected AmmoFactory(Vector2 _pos, float _range, float _mC, City _owner) {
		super(_pos, _range, _mC, _owner);
	}
}