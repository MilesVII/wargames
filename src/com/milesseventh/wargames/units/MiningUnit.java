package com.milesseventh.wargames.units;

import com.badlogic.gdx.math.Vector2;
import com.milesseventh.wargames.Territory;

public abstract class MiningUnit extends Unit {
	protected MiningUnit(Vector2 _pos, float _range, Territory _owner, float _mH) {
		super(_pos, _range, _owner, _mH);
	}

}
