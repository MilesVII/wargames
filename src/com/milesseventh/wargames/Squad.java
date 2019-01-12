package com.milesseventh.wargames;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.milesseventh.wargames.Heartstrings.SpecialTechnology;
import com.milesseventh.wargames.WG.Dialog;

public class Squad implements Piemenuable {
	public enum State {
		STAND, MOVING
	}
	
	private static final String[] SQUAD_NAMES = {"Alfa", "Bravo", "Charlie", "Delta", "Echo", "Foxtrot", "Golf", "Kilo", "Lima", "November", "Romeo", "Sierra", "Tango", "Uniform", "Victor", "Whiskey", "X-ray", "Yankee", "Zulu"};
	private static int nameSelector = 0;
	
	public Faction faction;
	public ArrayList<Unit> units = new ArrayList<Unit>();
	public String name;
	public ResourceStorage resources, tradePartner;
	private ArrayList<Structure> interactableStructures = new ArrayList<Structure>();
	
	private Vector2[] path = null;
	private int pathSegment = -1;
	public Vector2 position;
	public float lostDirection = 0;
	public State state = State.STAND;
	
	
	public Squad(Faction nowner, Vector2 nposition) {
		name = SQUAD_NAMES[nameSelector++];
		nameSelector %= SQUAD_NAMES.length;
		resources = new ResourceStorage("Squad " + name);
		
		//resources.add(Resource.FUEL, 2000f);
		
		position = nposition.cpy();
		faction = nowner;
		rebuildPiemenu();
		
		refuelSB = WG.antistatic.gui.new Scrollbar();
	}
	
	public void setPath(Vector2[] npath){
		if (npath.length > 1){
			path = npath.clone();
			pathSegment = -1;
			state = State.MOVING;
		}
	}
	
	public void resetPath(){
		path = null;
		state = State.STAND;
	}
	
	public void update(float dt){
		DEBUG_INFO = "Call from Squad.java: update():concentratePartial()";
		concentratePartial(Resource.FUEL);
		
		//Interaction
		if (WG.antistatic.getUIFromWorldV(position).dst(Utils.UIMousePosition) < WG.STRUCTURE_ICON_RADIUS * 1.2f &&
		    WG.antistatic.uistate == WG.UIState.FREE){
			if (state == State.MOVING)
				WG.antistatic.gui.path(path, 2, Color.BLACK, pathSegment);
			
			if (Gdx.input.justTouched())// && piemenu.size() > 0) it won't break anyway, and piemenu with only one action would be more demonstrative than nothing
				WG.antistatic.setFocusOnPiemenuable(this);
			
			WG.antistatic.gui.prompt(name + "\n" + units.size() + " units\n" + resources.get(Resource.FUEL) + Heartstrings.get(Resource.FUEL, Heartstrings.rProperties).sign + " of fuel\nPayload:" + getCapacity());
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
		
		spread();
		if (resources.sum() > 0){
			System.out.println("Spread not clear" + resources.sum());
		}
	}
	
	private float getSpeed(){
		assert(units.size() > 0);
		float min = Float.POSITIVE_INFINITY;
		for (Unit u: units)
			if (u.getSpeed() < min)
				min = u.getSpeed();
		return min;
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
		return resources.get(Resource.FUEL) > 0 || hasFuelNonTrading();
	}
	private boolean hasFuelNonTrading(){
		for (Unit u: units)
			if (u.resources.get(Resource.FUEL) > 0)
				return true;
		return false;
	}
	
	public float getFreeSpace(boolean isTrading){
		if (isTrading){
			return getCapacity() - resources.sum();
		} else {
			float r = 0;
			for (Unit u: units)
				r += u.getFreeSpace();
			return r;
		}
	}
	
	public float getCapacity(){
		float r = 0;
		for (Unit u: units)
			r += u.getCapacity();
		return r;
	}
	
	public float getMissileCapacity(){
		float r = 0;
		for (Unit u: units)
			r += u.getMissileCapacity();
		return r;
	}
	
	public void loadMissiles(ArrayList<Missile> missiles){
		assert(missiles.size() <= getMissileCapacity());
		
		for (int i = 0, j = 0; i < units.size() && j < missiles.size(); ++i){
			Unit u = units.get(i);
			
			while(u.getMissileCapacity() - u.missilesLoaded.size() > 0)
				u.missilesLoaded.add(missiles.get(j++));
		}
		
		missiles.clear();
	}
	
	public void unloadMissiles(ArrayList<Missile> receiver){
		for (Unit u: units){
			receiver.addAll(u.missilesLoaded);
			u.missilesLoaded.clear();
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////
	//Internal resource management
	public String DEBUG_INFO = "";
	public void prepareForTrading(){
		//assert(resources.sum() == 0);
		if (resources.sum() != 0){ //TODO: Replace with assert and remove DEBUG_INFO field when bug is catched
			System.out.println("ASSERT: Squad.java: prepareForTrading()");
			System.out.println(resources.sum());
			System.out.println(DEBUG_INFO);
			System.out.println("_______________________________________");
		}
		for (Unit u: units)
			u.resources.fullFlushTo(resources);
	}
	
	public void doneTrading(){
		for (Unit u: units){
			if (resources.sum() == 0)
				return;
			
			if (u.getFreeSpace() > 0)
				for (Resource r: Resource.values())
					if (resources.get(r) > 0)
						resources.tryTransfer(r, Math.min(u.getFreeSpace(), resources.get(r)), 
						                      u.resources);
		}
	}
	
	private void concentratePartial(Resource r){
		for (Unit u: units)
			u.resources.flushTo(r, resources);
	}
	
	private void spread(){doneTrading();}
	
	////////////////////////////////////////////////////////////////////////////////
	//Menus
	
	private ListEntryCallback LEC_BUILD_MENU = new ListEntryCallback(){
		@Override
		public void action(int id) {
			if (id < Structure.Type.values().length){
				Structure s = new Structure(position, Structure.Type.values()[id], faction);
				faction.registerStructure(s);
				
				Utils.displaceSomewhereWalkable(position, 
				                                Heartstrings.STRUCTURE_INTERACTION_DISTANCE2 * .2f * .2f, 
				                                WG.antistatic.map);
			}
			
			WG.antistatic.uistate = WG.UIState.FREE;
		}

		private boolean isEnoughMetalForBuilding(Structure.Type t){
			return resources.isEnough(Resource.METAL, Heartstrings.get(t, Heartstrings.structureProperties).buildingPrice);
		}
		@Override
		public void entry(Vector2 position, Vector2 size, int id, Color[] color) {
			String title, description = null;
			boolean buildingAllowed = true; 
			if (id < Structure.Type.values().length){
				Structure.Type st = Structure.Type.values()[id];

				
				prepareForTrading();
				switch(st){
				case CITY:
					if (!isEnoughMetalForBuilding(Structure.Type.CITY))
						buildingAllowed = false;
					break;
				case MINER:
					if (!isEnoughMetalForBuilding(Structure.Type.MINER))
						buildingAllowed = false;
					break;
				case MB:
					if (!faction.isInvestigated(SpecialTechnology.ADVANCED_WARFARE) ||
						!isEnoughMetalForBuilding(Structure.Type.MB))
						buildingAllowed = false;
					break;
				case AMD:
					if (!faction.isInvestigated(SpecialTechnology.AMD) ||
						!isEnoughMetalForBuilding(Structure.Type.AMD))
						buildingAllowed = false;
					break;
				case ML:
					if (!faction.isInvestigated(SpecialTechnology.STRATEGIC_WARFARE) ||
						!isEnoughMetalForBuilding(Structure.Type.ML))
						buildingAllowed = false;
					break;
				case RADAR:
					if (!faction.isInvestigated(SpecialTechnology.RADIO) ||
						!isEnoughMetalForBuilding(Structure.Type.RADAR))
						buildingAllowed = false;
					break;
				}
				doneTrading();
				
				description = Heartstrings.get(st, Heartstrings.structureProperties).description;
				title = Heartstrings.get(st, Heartstrings.structureProperties).title;
			} else
				title = "Cancel";
			
			WG.antistatic.gui.advancedButton(position, size, id, buildingAllowed ? this : GUI.GUI_ACT_DUMMY, 
			                                 color, title, description, buildingAllowed ? null : Color.GRAY); //TODO: Prompts cannot be shown in menu
		}
	};
	private ListEntryCallback LEC_DISBAND_SELECTION_MENU = new ListEntryCallback(){
		@Override
		public void action(int id) {
			if (id < interactableStructures.size() && 
			    interactableStructures.get(id).hasYard())
				disband(interactableStructures.get(id).yard,
				        interactableStructures.get(id).resources);
			
			WG.antistatic.uistate = WG.UIState.FREE;
		}

		@Override
		public void entry(Vector2 position, Vector2 size, int id, Color[] color) {
			String title;
			if (id < interactableStructures.size())
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
	private GUI.Scrollbar refuelSB;
	private ResourceStorage temporaryFuelTank = new ResourceStorage("KissingStallions");
	private ListEntryCallback LEC_REFUEL_MENU = new ListEntryCallback(){
		@Override
		public void action(int id) {
			refuelSB.initialized = false;
			WG.antistatic.uistate = WG.UIState.FREE;
		}

		@Override
		public void entry(Vector2 position, Vector2 size, int id, Color[] color) {
			Squad ns = Utils.findNearestSquad(faction, me.position, me);
			switch (id){
			case(0):
				WG.antistatic.gui.advancedButton(position, size, id, GUI.GUI_ACT_DUMMY, 
				                                 color, me.name + " - " + ns.name, null, null);
				break;
			case(1):
				//Refueling (Fuel-sharing) process
				//TODO: Probably the whole scrollbar should be operational, scaled into constrained zone. Rethink
				me.DEBUG_INFO = "Call from Squad.java: entry() of LEC_REFUEL_MENU";
				ns.DEBUG_INFO = "Call from Squad.java: entry() of LEC_REFUEL_MENU";
				me.prepareForTrading(); ns.prepareForTrading();
				
				boolean initThumb = false;
				if (!refuelSB.initialized){
					refuelSB.init(position, size, false, GUI.Scrollbar.GUI_SB_DEFAULT_THUMB);
					initThumb = true;
				}
				
				int states = (int)Math.floor(resources.get(Resource.FUEL) + ns.resources.get(Resource.FUEL)) + 1;
				int  leftConstraint = states - 1 - (int)Math.floor(ns.getFreeSpace(true) + ns.resources.get(Resource.FUEL));
				int rightConstraint = (int)Math.floor(me.getFreeSpace(true) + me.resources.get(Resource.FUEL));
				
				refuelSB.update(states);//update(rightConstraint - leftConstraint + 1)
				if (initThumb)
					refuelSB.offset = (int)Math.floor(me.resources.get(Resource.FUEL));
				refuelSB.offset = MathUtils.clamp(refuelSB.offset, leftConstraint, rightConstraint);
				
				assert(temporaryFuelTank.sum() == 0);
					me.resources.flushTo(Resource.FUEL, temporaryFuelTank);
					ns.resources.flushTo(Resource.FUEL, temporaryFuelTank);
					assert(temporaryFuelTank.tryTransfer(Resource.FUEL, refuelSB.offset, me.resources));
					temporaryFuelTank.flushTo(Resource.FUEL, ns.resources);
					//Case: Rounding error amount of fuel was flushed to non-transportable squad
					//TODO: Make sure that "me" (left side squad) cannot be receiver from abovementioned case 
					if (ns.resources.get(Resource.FUEL) > ns.getCapacity())
						ns.resources.tryTransfer(Resource.FUEL, 
						                         ns.resources.get(Resource.FUEL) - ns.getCapacity(), 
						                         me.resources);
				assert(temporaryFuelTank.sum() == 0);
				
				refuelSB.render(GUI.GUI_COLORS_SCROLLBAR_COLORS);
				
				me.doneTrading(); ns.doneTrading();
				
				//Caption over scrollbar
				WG.antistatic.gui.advancedButton(position, size, id, GUI.GUI_ACT_DUMMY, GUI.GUI_COLORS_TRANSPARENT, 
				                                 "" + refuelSB.offset + "/" + (states - 1 - refuelSB.offset), null, null);
				break;
			case(2):
				WG.antistatic.gui.advancedButton(position, size, id, this, color, "Done", null, null);
				break;
			}
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
			Utils.findStructuresWithinRadius2(interactableStructures, faction, position, Heartstrings.STRUCTURE_INTERACTION_DISTANCE2, null);
			
			if (isUnitTypePresent(Unit.Type.BUILDER) &&
			    Utils.isOkToBuild(position))
				piemenu.add(PME_BUILD);
			
			if (interactableStructures.size() > 0){
				if (isUnitTypePresent(Unit.Type.TRANSPORTER))
					piemenu.add(PME_TRADE);
				
				for (Structure s: interactableStructures)
					if (s.hasYard()){
						piemenu.add(PME_DISBAND);
						break;
					}
			}
			
			Squad ns = Utils.findNearestSquad(faction, position, this);
			if (ns != null &&
			    position.dst2(ns.position) <= Heartstrings.STRUCTURE_INTERACTION_DISTANCE2 && 
			    (hasFuel() || ns.hasFuel()) && ns.state == State.STAND){
				piemenu.add(PME_REFUEL);
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
				disband(interactableStructures.get(0).yard,
				        interactableStructures.get(0).resources);
			else
				WG.antistatic.setMenu(LEC_DISBAND_SELECTION_MENU, interactableStructures.size() + 1);
		}
	});
	public final PiemenuEntry PME_REFUEL = new PiemenuEntry("Refuel", new Callback(){
		@Override
		public void action(int source) {
			WG.antistatic.setMenu(LEC_REFUEL_MENU, 3);
		}
	});
	private void disband(ArrayList<Unit> to, ResourceStorage rs){
		prepareForTrading();
		resources.fullFlushTo(rs);
		to.addAll(units);
		faction.squads.remove(me);
		
	}
	private void trade(ResourceStorage rs){
		//WG.antistatic.gui.focusedStruct = Utils.findNearestStructure(faction, position, null);
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
