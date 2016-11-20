package com.milesseventh.wargames;

import java.util.Random;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;

public class Mountain{
	private static Random r = new Random();
	private Circle circle;
	
	public Mountain (Vector2 _pos, float _minR, float _maxR){
		float _radius = r.nextFloat() * (_maxR - _minR) + _minR;
		circle = new Circle(_pos.x, _pos.y, _radius);
	}
}
