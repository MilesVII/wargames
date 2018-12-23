package com.milesseventh.wargames;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.milesseventh.wargames.WG.Dialog;

public class Squad implements Piemenuable {
	public enum State {
		STAND, MOVING
	}
	
	private static final String[] SQUAD_NAMES = {"Alfa", "Bravo", "Charlie", "Delta", "Echo", "Foxtrot", "Golf", "Kilo", "Lima", "November", "Romeo", "Sierra", "Tango", "Uniform", "Victor", "Whiskey", "X-ray", "Yankee", "Zulu"};
	private static int nameSelector = 0;
	
	public Faction owner;
	public ArrayList<Unit> units = new ArrayList<Unit>();
	public String name;
	public ResourceStorage resources, tradePartner;
	private ArrayList<Structure> interactableStructures = new ArrayList<Structure>();
	
	private Vector2[] path = null;
	private int pathSegment = -1;
	public Vector2 position;
	public float lostDirection = 0;
	public State state = State.STAND;
	
	public static final float STRUCTURE_INTERACTION_DISTANCE2 = 120;
	
	public Squad(Faction nowner, Vector2 nposition) {
		name = SQUAD_NAMES[nameSelector++];
		nameSelector %= SQUAD_NAMES.length;
		resources = new ResourceStorage("Squad " + name);
		
		resources.add(Resource.FUEL, 2000f);
		
		position = nposition.cpy();
		owner = nowner;
		rebuildPiemenu();
	}
	
	public void setPath(Vector2[] npath){
		path = npath.clone();
		pathSegment = -1;
				state = State.MOVING;
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
			
			if (Gdx.input.justTouched())// && piemenu.size() > 0) it won't break anyway, and piemenu with only one action would be more demonstrative than nothing
				WG.antistatic.setFocusOnPiemenuable(this);
			
			WG.antistatic.gui.prompt(name + "\n" + units.size() + " units\n" + resources.get(Resource.FUEL) + Heartstrings.get(Resource.FUEL, Heartstrings.rProperties).sign + " of fuel");
		}
		
		//Move column on path
		if (!hasFuel())
			resetPath();
		
		Vector2 positionHolder = Utils.getVector(position);
		if (path != null && path.length > 1){
			if (pathSegment == -1){
				pathSegment = 0;
			} else {
				float step = getSpeed() * dt;
				
				float fuelWasted = step * getFuelConsumption();
				System.out.println(fuelWasted);
				if (resources.isEnough(Resource.FUEL, fuelWasted))
					resources.tryRemove(Resource.FUEL, fuelWasted);
				else {
					step = step * resources.get(Resource.FUEL) / fuelWasted;
					resources.set(Resource.FUEL, 0);
				}
				
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
	
	public float getFuelConsumption(){
		float fc = 0;
		for (Unit u: units)
			fc += u.getFuelConsumption();
		return fc;
	}
	
	public boolean hasFuel(){
		return resources.get(Resource.FUEL) > 0;
	}
	
	public float getCapacity(){
		float r = 0;
		for (Unit u: units)
			if (u.type == Unit.Type.TRANSPORTER)
				r += u.getCapacity();
		return r;
	}
	
	private ListEntryCallback LEC_BUILD_MENU = new ListEntryCallback(){
		@Override
		public void action(int id) {
			if (id < Structure.Type.values().length){
				Structure s = new Structure(position, Structure.Type.values()[id], owner);
				Faction.debug.registerStructure(s);
				s.yard.addAll(units);
				owner.squads.remove(me);
			}
			
			WG.antistatic.uistate = WG.UIState.FREE;
		}

		@Override
		public void entry(Vector2 position, Vector2 size, int id, Color[] color) {
			String title;
			if (id < Structure.Type.values().length){
				Structure.Type st = Structure.Type.values()[id];
				title = Heartstrings.get(st, Heartstrings.structureProperties).title;
			} else
				title = "Cancel";
			
			WG.antistatic.gui.advancedButton(position, size, id, this, color, title, null, null);
		}
	};
	private ListEntryCallback LEC_DISBAND_SELECTION_MENU = new ListEntryCallback(){
		@Override
		public void action(int id) {
			if (id < Structure.Type.values().length)
				join(interactableStructures.get(id).yard);
			
			WG.antistatic.uistate = WG.UIState.FREE;
		}

		@Override
		public void entry(Vector2 position, Vector2 size, int id, Color[] color) {
			String title;
			if (id < Structure.Type.values().length)
				title = interactableStructures.get(id).name;
			else
				title = "Cancel";
			
			WG.antistatic.gui.advancedButton(position, size, id, this, color, title, null, null);
		}
	};
	private ListEntryCallback LEC_TRADE_SELECTION_MENU = new ListEntryCallback(){
		@Override
		public void action(int id) {
			if (id < Structure.Type.values().length)
				trade(interactableStructures.get(id).resources);
			
			WG.antistatic.uistate = WG.UIState.FREE;
		}

		@Override
		public void entry(Vector2 position, Vector2 size, int id, Color[] color) {
			String title;
			if (id < Structure.Type.values().length)
				title = interactableStructures.get(id).name;
			else
				title = "Cancel";
			
			WG.antistatic.gui.advancedButton(position, size, id, this, color, title, null, null);
		}
	};
	
	//Piemenu implementation
	public final ArrayList<PiemenuEntry> piemenu = new ArrayList<PiemenuEntry>();
	private void rebuildPiemenu(){
		piemenu.clear();
		piemenu.add(PiemenuEntry.PME_CANCEL);
		if (state == State.MOVING)
			piemenu.add(PME_STOP);
		else if (hasFuel())
			piemenu.add(PME_MOVE);
		
		if (state == State.STAND){
			//Structure nfs = Utils.findNearestStructure(owner, position, null);
			Utils.findStructuresWithinRadius2(interactableStructures, owner, position, STRUCTURE_INTERACTION_DISTANCE2, null);
			
			if (isUnitTypePresent(Unit.Type.BUILDER))
				piemenu.add(PME_BUILD);
			
			if (interactableStructures.size() > 0){
				if (isUnitTypePresent(Unit.Type.TRANSPORTER))
					piemenu.add(PME_TRADE);
				piemenu.add(PME_DISBAND);
			}
		}
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
			assert(interactableStructures.size() > 0);
			
			if (interactableStructures.size() == 1)
				trade(interactableStructures.get(0).resources);
			else
				WG.antistatic.setMenu(LEC_TRADE_SELECTION_MENU, interactableStructures.size() + 1);
		}
	});
	public final PiemenuEntry PME_BUILD = new PiemenuEntry("Build", new Callback(){
		@Override
		public void action(int source) {
			WG.antistatic.setMenu(LEC_BUILD_MENU, Structure.Type.values().length + 1);
		}
	});
	private Squad me = this;
	public final PiemenuEntry PME_DISBAND = new PiemenuEntry("Disband", new Callback(){
		@Override
		public void action(int source) {
			assert(interactableStructures.size() > 0);
			
			if (interactableStructures.size() == 1)
				join(interactableStructures.get(0).yard);
			else
				WG.antistatic.setMenu(LEC_DISBAND_SELECTION_MENU, interactableStructures.size() + 1);
		}
	});
	private void join(ArrayList<Unit> to){
		to.addAll(units);
		owner.squads.remove(me);
	}
	
	private void trade(ResourceStorage rs){
		//WG.antistatic.gui.focusedStruct = Utils.findNearestStructure(owner, position, null);
		tradePartner = rs;
		WG.antistatic.openDialog(Dialog.TRADE);
	}
	//Piemenuable interface
	@Override
	public Vector2 getWorldPosition() {
		return position;
	}
	
	@Override
	public ArrayList<PiemenuEntry> getEntries() {
		return piemenu;
	}
}
