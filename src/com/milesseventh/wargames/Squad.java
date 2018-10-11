package com.milesseventh.wargames;

import java.util.ArrayList;

import com.badlogic.gdx.math.Vector2;

public class Squad {
	public Fraction owner;
	public ArrayList<Unit> units;
	
	private Vector2[] path = null;
	private int pathSegment = -1;
	public Vector2 position;
	
	public Squad(Fraction nowner, Vector2 nposition) {
		position = nposition;
		owner = nowner;
	}
	
	public void setPath(Vector2[] npath){
		path = npath;
		pathSegment = -1;
	}
	
	public void update(float dt){
		
		//Move column on path
		if (path != null && path.length > 1){
			if (pathSegment == -1){
				pathSegment = 0;
				dt = 0; // Remove shift when starting to move
			}
			float step = getSpeed() * dt;
			while(step >= position.dst(path[pathSegment + 1])){
				step -= position.dst(path[pathSegment + 1]);
				position = path[pathSegment + 1];
				++pathSegment;

				if (pathSegment == path.length - 1){
					//Arrived!
					path = null;
					pathSegment = -1;
					step = 0;
					break;
				}
			}
			
			if (pathSegment > -1)
				position.add(Utils.getVector(path[pathSegment + 1])
				             	.sub(position)
				             	.nor().scl(step));
		}
	}
	
	private float getSpeed(){
		return 170; //TODO: Stub
	}
}
