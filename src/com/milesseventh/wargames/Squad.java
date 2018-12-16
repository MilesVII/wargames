package com.milesseventh.wargames;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

public class Squad implements Piemenuable {
	public enum State {
		STAND, MOVING
	}
	
	public Faction owner;
	public ArrayList<Unit> units = new ArrayList<Unit>();
	
	private Vector2[] path = null;
	private int pathSegment = -1;
	public Vector2 position;
	public float lostDirection = 0;
	public State state = State.STAND;
	
	public Squad(Faction nowner, Vector2 nposition) {
		position = nposition.cpy();
		owner = nowner;
		rebuildPiemenu();
	}
	
	public void setPath(Vector2[] npath){
		path = npath.clone();
		pathSegment = -1;
	}
	
	public void resetPath(){
		path = null;
		state = State.STAND;
	}
	
	public void update(float dt){
		//Interaction
		if (WG.antistatic.getUIFromWorldV(position).dst(Utils.UIMousePosition) < WG.STRUCTURE_ICON_RADIUS * 1.2f &&
			WG.antistatic.uistate == WG.UIState.FREE){
			if (state == State.MOVING)
				WG.antistatic.gui.path(path, 2, Color.BLACK, pathSegment);
			if (Gdx.input.justTouched()){
				WG.antistatic.setFocusOnPiemenuable(this);
			}
			WG.antistatic.gui.prompt("Squad!");
		}
		
		//Move column on path
		Vector2 positionHolder = Utils.getVector(position);
		if (path != null && path.length > 1){
			if (pathSegment == -1){
				pathSegment = 0;
			} else {
				state = State.MOVING;
				
				float step = getSpeed() * dt;
				while(step >= position.dst(path[pathSegment + 1])){
					++pathSegment;
					step -= position.dst(path[pathSegment]);
					position = path[pathSegment].cpy();
	
					if (pathSegment == path.length - 1){
						//Arrived!
						state = State.STAND;
						
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
		rebuildPiemenu();
	}
	
	private float getSpeed(){
		return 7f; //TODO: Stub
	}
	
	public boolean isUnitTypePresent(Unit.Type type){
		for (Unit u: units)
			if (u.type == type)
				return true;
		return false;
	}
	
	//Piemenu implementation
	public final ArrayList<PiemenuEntry> PIEMENU = new ArrayList<PiemenuEntry>();
	private void rebuildPiemenu(){
		PIEMENU.clear();
		PIEMENU.add(PiemenuEntry.PME_CANCEL);
		if (state == State.MOVING)
			PIEMENU.add(PME_STOP);
		else
			PIEMENU.add(PME_MOVE);
		if (state == State.STAND && isUnitTypePresent(Unit.Type.TRANSPORTER))
			PIEMENU.add(PME_TRADE);
		if (state == State.STAND && isUnitTypePresent(Unit.Type.BUILDER))
			PIEMENU.add(PME_BUILD);
	}

	public final PiemenuEntry PME_MOVE = new PiemenuEntry("Move", new Callback(){
		@Override
		public void action(int source) {
			WG.antistatic.uistate = WG.UIState.MOVINGORDER;
		}
	});
	public final PiemenuEntry PME_STOP = new PiemenuEntry("Stop", new Callback(){
		@Override
		public void action(int source) {
			resetPath();
		}
	});
	public final PiemenuEntry PME_TRADE = new PiemenuEntry("Trade", new Callback(){
		@Override
		public void action(int source) {
			//TODO: Stub
		}
	});
	public final PiemenuEntry PME_BUILD = new PiemenuEntry("Build", new Callback(){
		@Override
		public void action(int source) {
			//TODO: Stub
		}
	});
	
	//Piemenuable interface
	@Override
	public Vector2 getWorldPosition() {
		return position;
	}
	
	@Override
	public ArrayList<PiemenuEntry> getEntries() {
		return PIEMENU;
	}
}
