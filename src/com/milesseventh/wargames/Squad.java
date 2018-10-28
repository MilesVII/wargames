package com.milesseventh.wargames;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

public class Squad implements Piemenuable {
	public Faction owner;
	public ArrayList<Unit> units;
	
	private Vector2[] path = null;
	private int pathSegment = -1;
	public Vector2 position;
	public float lostDirection = 0;
	
	public Squad(Faction nowner, Vector2 nposition) {
		position = nposition.cpy();
		owner = nowner;
	}
	
	public void setPath(Vector2[] npath){
		path = npath.clone();
		pathSegment = -1;
	}
	
	public void update(float dt){
		Vector2 positionHolder = Utils.getVector(position);
		//Move column on path
		if (path != null && path.length > 1){
			if (pathSegment == -1){
				pathSegment = 0;
			} else {
				float step = getSpeed() * dt;
				while(step >= position.dst(path[pathSegment + 1])){
					++pathSegment;
					step -= position.dst(path[pathSegment]);
					position = path[pathSegment].cpy();
	
					if (pathSegment == path.length - 1){
						//Arrived!
						path = null;
						pathSegment = -1;
						step = 0;
						break;
					}
				}
				
				if (pathSegment > -1){
					Vector2 offset = Utils.getVector(path[pathSegment + 1]).sub(position).nor().scl(step);
					position.add(offset);
				}
				lostDirection = Utils.getVector(position).sub(positionHolder).angle();
			}
		}
	}
	
	private float getSpeed(){
		return 7f; //TODO: Stub
	}
	
	//Piemenuable interface implementation
	@Override
	public Vector2 getWorldPosition() {
		return position;
	}

	@Override
	public int getActionsAmount() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public Callback getAction() {
		// TODO Auto-generated method stub
		return null;
	}
}
