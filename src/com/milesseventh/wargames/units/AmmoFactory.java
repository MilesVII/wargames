package com.milesseventh.wargames.units;

import com.badlogic.gdx.math.Vector2;
import com.milesseventh.wargames.Territory;

public class AmmoFactory extends IndustrialUnit {
	protected float MAX_IP = 5;
	protected int MAX_LEVEL = 2;
	protected AmmoFactory(Vector2 _pos, float _range, Territory _owner, float _mH) {
		super(_pos, _range, _owner, _mH);
	}

	
}
