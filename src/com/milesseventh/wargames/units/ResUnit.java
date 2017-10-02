package com.milesseventh.wargames.units;

import com.badlogic.gdx.math.Vector2;

public class ResUnit extends Unit {
	City ownerCity;
	
	protected ResUnit(Vector2 _pos, float _range, float _mC, City owner) {
		super(_pos, _range, _mC, owner.getFraction());
		ownerCity = owner;
	}

	public City getOwnerCity(){
		return ownerCity;
	}

	@Override
	public void unregister() {
		ownerCity.slaves.remove(this);
	}
}
