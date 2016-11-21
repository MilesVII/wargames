package com.milesseventh.wargames.units;

import com.badlogic.gdx.math.Vector2;
import com.milesseventh.wargames.*;

public abstract class Unit {
	private float ownRange;//Radius of circle that will be added to fraction's territory
	private Territory owner;//ID of fraction that owns this unit
	private float health, maxHealth;
	private Vector2 position;
	
	protected Unit(Vector2 _pos, float _range, Territory _owner, float _mH){
		ownRange = _range;
		position = _pos;
		owner = _owner;
		health = maxHealth = _mH;
	}
	
	public float getOwnRange(){
		return (ownRange);
	}

	public void hit(float _damage){
		health -= _damage;
		if (health <= 0f)
			owner.unregister(this);
	}
	
	public void repair(float _repair){
		health += _repair;
		if (health > maxHealth){
			health = maxHealth;
		}
	}
	
	public Territory getOwner(){
		return owner;
	}

	public Vector2 getPosition(){
		return position;
	}
	////
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
