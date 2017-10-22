package com.milesseventh.wargames;

import java.util.ArrayList;

import com.badlogic.gdx.math.Vector2;

public class Structure{
	///////////////////////////////////////
	//MERGED WITH StationaryUnit.java
	//TO BE REVIEWED
	
	//private ArrayList<Mine> mines = new ArrayList<Mine>();
	private float[] resources = {0, 0, 0};
	public static final int RES_ORE = 0, RES_MET = 1, RES_AMMO = 2;
	public static final float DEFAULT_RANGE = 27, DEFAULT_CONDITION = 100;
	
	//Imported block
	private float ownRange;//Radius of circle that will be added to fraction's territory
	private Fraction ownerFraction;//ID of fraction that owns this unit
	private float condition, maxCondition;
	private Vector2 position;
	/////////////////
	
	public Structure(Vector2 _pos, Fraction _owner) {
		position = _pos;
		ownerFraction = _owner;
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
	
	/*public void addMine(Mine _m){
		mines.add(_m);
	}
	
	public void mineDestroyed(Mine _m){
		mines.remove(_m);
	}*/
	
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
	
	//Imported block
	public void changeOwner(Fraction _newMaster){
		ownerFraction = _newMaster;
	}
	
	public Fraction getFraction(){
		return ownerFraction;
	}
	
	public void onDestroy() {
		ownerFraction.unregisterStructure(this);
		//And do some unique city destructive stuff
	}
	
	public float getOwnRange(){
		return (ownRange);
	}
	
	public void hit(float _damage){
		condition -= _damage;
		if (condition <= 0){
			onDestroy();
		}
	}
	
	public void repair(float _repair){
		condition += _repair;
		if (condition > maxCondition){
			condition = maxCondition;
		}
	}
	
	public Vector2 getPosition(){
		return position;
	}

	public void setPosition(Vector2 _n){
		position = _n;
	}
	
	public float getX(){
		return position.x;
	}
	
	public float getY(){
		return position.y;
	}
}
