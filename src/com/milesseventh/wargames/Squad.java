package com.milesseventh.wargames;

import java.util.ArrayList;

import com.badlogic.gdx.math.Vector2;

public class Squad {
	public Faction owner;
	public ArrayList<Unit> units;
	
	private Vector2[] path = null;
	private int pathSegment = -1;
	public Vector2 position;
	
	public Squad(Faction nowner, Vector2 nposition) {
		position = nposition.cpy();
		owner = nowner;
	}
	
	public void setPath(Vector2[] npath){
		path = npath.clone();
		pathSegment = -1;
	}
	
	public void update(float dt){
		//Move column on path
		System.out.print("Preupdate:");
		System.out.println(position);
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
					System.out.print("Prestep:  ");
					System.out.println(position);
					Vector2 offset = Utils.getVector(path[pathSegment + 1]).sub(position).nor().scl(step);
					position.add(offset);
					System.out.print("Poststep: ");
					System.out.println(position);
				}
			}
		}
	}
	
	private float getSpeed(){
		return 7f; //TODO: Stub
	}
}
