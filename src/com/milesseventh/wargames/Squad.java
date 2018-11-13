package com.milesseventh.wargames;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.milesseventh.wargames.WG.Dialog;

public class Squad implements Piemenuable {
	public Faction owner;
	public ArrayList<Unit> units = new ArrayList<Unit>();
	
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
		if (WG.antistatic.getUIFromWorldV(position).dst(Utils.UIMousePosition) < WG.STRUCTURE_ICON_RADIUS * 1.2f){
			if (pathSegment != -1)
				WG.antistatic.gui.path(path, 2, Color.BLACK, pathSegment);
			if (Gdx.input.justTouched()){
				WG.antistatic.setFocusOnPiemenuable(this);
			}
		}
		
		//Move column on path
		Vector2 positionHolder = Utils.getVector(position);
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
	
	public static final Callback PIEMENU_ACTIONS = new Callback(){
		@Override
		public void action(int source) {
			switch(source){
			case(0):
				//System.out.println("Cancelled");
				break;
			case(1):
				//Move
				WG.antistatic.uistate = WG.UIState.MOVINGORDER;
				break;
			case(2):
				//Trade
				break;
			}
		}
	};
	
	private static final String[] PIEMENU_CAPTIONS = {
		"Cancel", "Move", "Share"
	};
	
	//Piemenuable interface implementation
	@Override
	public Vector2 getWorldPosition() {
		return position;
	}

	@Override
	public int getActionsAmount() {
		// TODO Auto-generated method stub
		return 3;
	}

	@Override
	public Callback getAction() {
		return PIEMENU_ACTIONS;
	}
	
	@Override
	public String[] getCaptions(){
		return PIEMENU_CAPTIONS;
	}
}
