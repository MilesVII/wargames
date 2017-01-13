package com.milesseventh.wargames.units;

import com.badlogic.gdx.math.Vector2;
import com.milesseventh.wargames.*;

public abstract class Unit {
	private float ownRange;//Radius of circle that will be added to fraction's territory
	private Fraction ownerFraction;//ID of fraction that owns this unit
	private float condition, maxCondition;
	private Vector2 position;
	
	protected Unit(Vector2 _pos, float _range, float _mC, Fraction owner){
		ownRange = _range;
		ownerFraction = owner;
		position = _pos;
		condition = maxCondition = _mC;
	}
	
	public float getOwnRange(){
		return (ownRange);
	}

	public abstract void unregister();
	
	public void hit(float _damage){
		condition -= _damage;
		if (condition <= 0){
			unregister();
		}
	}
	
	public void repair(float _repair){
		condition += _repair;
		if (condition > condition){
			condition = condition;
		}
	}

	public Fraction getFraction(){
		return ownerFraction;
	}
	
	public Vector2 getPosition(){
		return position;
	}
	/////////
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
