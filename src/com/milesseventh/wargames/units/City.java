package com.milesseventh.wargames.units;

import java.util.ArrayList;

import com.badlogic.gdx.math.Vector2;
import com.milesseventh.wargames.Fraction;

public class City extends StationaryUnit {
	private ArrayList<Mine> mines = new ArrayList<Mine>();
	private float[] resources = {0, 0, 0};
	public static final int RES_ORE = 0, RES_MET = 1, RES_AMMO = 2;
	public static final float DEFAULT_RANGE = 27, DEFAULT_CONDITION = 100;
	private Fraction ownerFraction;

	public City(Vector2 _pos, Fraction _owner) {
		this(_pos, DEFAULT_RANGE, DEFAULT_CONDITION, _owner);
	}
	
	public City(Vector2 _pos, float _range, float _mC, Fraction _owner) {
		super(_pos, _range, _mC, _owner);
	}

	/*public float getGroupResource(int _resType){
		if (_resType >= 0 && _resType < 3)
			//TO: Recursively sum up resources of all neighbours
			return 0;
		else {
			System.err.println("City.java: Wrong _resType");
			return 0;
		}
	}*/
	
	public void addMine(Mine _m){
		mines.add(_m);
	}
	
	public void mineDestroyed(Mine _m){
		mines.remove(_m);
	}
	
	public float getResource(int _resType){
		if (_resType >= 0 && _resType < 3)
			return resources[_resType];
		else {
			System.err.println("City.java: Wrong _resType");
			return 0;
		}
	}
	
	public void addResource(int _resType, float _add){
		if (_resType >= 0 && _resType < 3)
			resources[_resType] += _add;
	}
	
	public boolean tryRemoveResource(int _resType, float _subtr){
		if (_resType >= 0 && _resType < 3)
			if (resources[_resType] >= _subtr){
				resources[_resType] -= _subtr;
				return true;
			} else
				return false;
		return false;
	}
	
	public void changeOwner(Fraction _newMaster){
		ownerFraction = _newMaster;
	}
	
	public Fraction getFraction(){
		return ownerFraction;
	}

	@Override
	public void hit(float _damage) {
		super.hit(_damage);
	}

	@Override
	public void onDestroy() {
		ownerFraction.unregisterCity(this);
		//And do some unique city destructive stuff
	}
}
