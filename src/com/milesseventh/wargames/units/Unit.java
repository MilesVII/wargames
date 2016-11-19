package com.milesseventh.wargames.units;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;
import com.milesseventh.wargames.Marchable;
import com.milesseventh.wargames.Territory;

public abstract class Unit implements Marchable{
	private float ownRange;//Radius of circle that will be added to fraction's territory
	private Territory owner;//ID of fraction that owns this unit
	private float health, maxHealth;
	private Vector2 position;
	private Circle circle;
	
	protected Unit(Vector2 _pos, float _range, Territory _owner, float _mH){
		position = _pos;
		ownRange = _range;
		circle = new Circle(_pos.x, _pos.y, _range);
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
	
	@Override
	public Circle getCircle(){
		return circle;
	}
}
