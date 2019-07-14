package com.milesseventh.wargames;

import java.util.ArrayList;
import java.util.Comparator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.milesseventh.wargames.Heartstrings.SpecialTechnology;
import com.milesseventh.wargames.Heartstrings.Technology;
import com.milesseventh.wargames.WG.Dialog;

public class Squad implements Piemenuable, Combatant, Tradeable {
	public enum State {
		STAND, MOVING, FORTIFIED
	}

	public static Comparator<Unit> sortByCapacity = new Comparator<Unit>(){
		@Override
		public int compare(Unit u0, Unit u1) {
			float cargo0 = u0.type == Unit.Type.TRANSPORTER ? u0.getCapacity() : Float.POSITIVE_INFINITY;
			float cargo1 = u1.type == Unit.Type.TRANSPORTER ? u1.getCapacity() : Float.POSITIVE_INFINITY;
			
			return (int)Math.signum(cargo0 - cargo1);
		}
	};
	
	private static final String[] SQUAD_NAMES = {"Alfa", "Bravo", "Charlie", "Delta", "Echo", "Foxtrot", "Golf", "Kilo", "Lima", "November", "Romeo", "Sierra", "Tango", "Uniform", "Victor", "Whiskey", "X-ray", "Yankee", "Zulu"};
	private static int nameSelector = 0;
	
	public Faction faction;
	public ArrayList<Unit> units = new ArrayList<Unit>();
	public String name;
	public ResourceStorage resources;
	public boolean trading = false;
	public boolean destroyed = false;
	private ArrayList<Structure> interactableStructures = new ArrayList<Structure>();
	private ArrayList<Tradeable> potentialTradeables = new ArrayList<Tradeable>();
	private ArrayList<Combatant> targets = new ArrayList<Combatant>();
	private ArrayList<Squad> TSqTargets = new ArrayList<Squad>();         // Temporary target lists for two enemy types
	private ArrayList<Structure> TStTargets = new ArrayList<Structure>(); 
	private float fortification = 0;
	
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
		//concentratePartial(Resource.FUEL);
		beginTrade();
		
		//Interaction
		if (WG.antistatic.getUIFromWorldV(position).dst(Utils.UIMousePosition) < WG.STRUCTURE_ICON_RADIUS * 1.2f &&
		    WG.antistatic.uistate == WG.UIState.FREE){
			if (state == State.MOVING)
				WG.antistatic.gui.path(path, 2, Color.BLACK, pathSegment);
			
			if (Gdx.input.justTouched())// && piemenu.size() > 0) it won't break anyway, and piemenu with only one action would be more demonstrative than nothing
				WG.antistatic.setFocusOnPiemenuable(this);
			
			WG.antistatic.gui.prompt(name + "\n" + units.size() + " units\n" + (resources.get(Resource.FUEL) / WG.VIRTUAL_FRACTION_SIZE) + Heartstrings.get(Resource.FUEL, Heartstrings.rProperties).sign + " of fuel\nPayload:" + getCapacity());
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
				
				int fuelWasted = Math.round(step * getFuelConsumption());
				
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
		
		//Fortification
		if (fortification <= 1)
			fortification += Heartstrings.SQUAD_FORTIFICATION_PER_SECOND * dt;
		
		//Fight
		if (isUnitTypePresent(Unit.Type.FIGHTER) && resources.get(Resource.AMMO) > 0 &&
		    (state == State.STAND || faction.isInvestigated(SpecialTechnology.MOBILE_ATTACK))){
			fight(dt);
		}
		rebuildPiemenu();
		
		spread();
		assert(resources.sum() == 0);
		/*if (resources.sum() > 0){
			System.out.println("Spread not clear" + resources.sum());
		}*/
	}
	
	@Override
	public void receiveFire(float power) {
		if (units.size() == 0)
			return;
		
		//Count sum of chances for types to get hit when squad is attacked
		float chancesSum = 0;
		for (Unit.Type t: Unit.Type.values())
			if (isUnitTypePresent(t))
				chancesSum += Heartstrings.get(t, Heartstrings.uProperties).getReceiveFireChance();
		
		//Select type of unit that will be damaged
		float r = Utils.random.nextFloat();
		float i = 0;
		Unit.Type victimType = null;
		for (Unit.Type t: Unit.Type.values())
			if (isUnitTypePresent(t)){
				i += Heartstrings.get(t, Heartstrings.uProperties).getReceiveFireChance() / chancesSum;
				if (r < i){
					victimType = t;
					break;
				}
			}
		
		//Find the exact unit that will be damaged by random id
		assert(victimType != null);
		int victimId = Utils.random.nextInt(countUnitsOfType(victimType));
		i = 0;
		Unit finalVictim = null;
		float debrisDamage = -1;
		for (Unit u: units){
			if (u.type == victimType && i++ == victimId){
				//Change hit power if fortified
				if (state == State.FORTIFIED && u.st.contains(Heartstrings.SpecialTechnology.FORTIFICATION))
					if (fortification <= 1)
						power *= 2.5f;
					else 
						power *= .2f;
				
				finalVictim = u;
				debrisDamage = u.receiveDamage(power);
				break;
			}
		}
		
		//Process debris damage, if unit was destroyed
		assert(finalVictim != null);
		if (debrisDamage > 0){
			destroyUnit(finalVictim);
			receiveFire(debrisDamage);
		}
	}
	
	private void fight(float dt){
		float maxAttackRange2 = getMaxAttackRange2();
		
		targets.clear();
		
		for (Faction f: Faction.factions){
			if (f == faction)
				continue;
			
			Utils.findStructuresWithinRadius2(TStTargets, true, f, position, maxAttackRange2, null);
			Utils.findSquadsWithinRadius2(TSqTargets, true, f, position, maxAttackRange2, this);
			
			targets.addAll(TSqTargets);
			targets.addAll(TStTargets);
			
		}
		if (targets.size() > 0){
			for (Unit u: units){
				if (u.type == Unit.Type.FIGHTER){
					/* Well, listen
					 * Here I iterate over every unit in squad
					 * Then checking, how many of detected /targets/ their gun can reach
					 * Then removing unit's regular amount of ammo for attack
					 * And splitting it's power on every target reachable*/
					
					int targetsWithinRange = 0;
					for (Combatant target: targets)
						if (target.getPosition().dst2(position) <= u.getAttackRange2())
							++targetsWithinRange;
					
					float bullets = dt; // TODO: Amount of ammo wasted on attack
					bullets = Math.min(bullets, resources.get(Resource.AMMO));
					boolean a = resources.tryRemove(Resource.AMMO, Math.round(bullets * WG.VIRTUAL_FRACTION_SIZE));
					assert(a);
					
					for (Combatant target: targets)
						if (target.getPosition().dst2(position) <= u.getAttackRange2()){
							target.receiveFire(u.getFirepower() * bullets / targetsWithinRange);
							if (u.st.contains(SpecialTechnology.SIEGE) &&
							    target.getClass().isAssignableFrom(Structure.class)){
								tryAnnexate((Structure)target, u, dt);
							}
						}
				}
			}
		}
	}
	
	private void tryAnnexate(Structure s, Unit u, float dt){
		if (s.condition <= s.getMaxCondition() * Heartstrings.STRUCTURE_CONDITION_ANNEXATION_THRESHOLD_RELATIVE &&
		    Heartstrings.UNIT_MAX_ANNEXATION_CHANCE_PER_SECOND * Heartstrings.get(Technology.ACCURACY, u.techLevel) * dt > Utils.random.nextFloat())
			s.annexated(faction);
	}
	
	public void destroyUnit(Unit u){
		assert(units.contains(u));
		assert(u.condition <= 0);
		boolean wasTrading = trading;
		
		if (wasTrading)
			endTrade();
		Utils.dropResources(position, u.resources);
		units.remove(u);
		if (wasTrading)
			beginTrade();
		
		if (units.size() == 0)
			destroyed = true;
	}
	
	@Override
	public Vector2 getPosition(){
		return position;
	}
	
	private float getSpeed(){
		assert(units.size() > 0);
		float min = Float.POSITIVE_INFINITY;
		for (Unit u: units)
			if (u.getSpeed() < min)
				min = u.getSpeed();
		return min;
	}

	public float getMaxCondition(){
		float r = 0;
		for (Unit u: units)
			r += u.getMaxCondition();
		return r;
	}
	
	public float getCondition(){
		float r = 0;
		for (Unit u: units)
			r += u.condition;
		return r;
	}
	
	public float getMaxAttackRange(){
		return (float)Math.sqrt(getMaxAttackRange2());
	}
	
	private float getMaxAttackRange2(){
		float maxFightingRadius = 0;
		for (Unit u: units){
			float ar2 = u.getAttackRange2();
			if (u.type == Unit.Type.FIGHTER && maxFightingRadius < ar2)
				maxFightingRadius = ar2;
		}
		
		return maxFightingRadius;
	}
	
	public boolean isUnitTypePresent(Unit.Type type){
		for (Unit u: units)
			if (u.type == type)
				return true;
		return false;
	}
	
	public int countUnitsOfType(Unit.Type type){
		int r = 0;
		for (Unit u: units)
			if (u.type == type)
				++r;
		return r;
	}
	
	public boolean canFortify(){
		for (Unit u: units)
			if (u.type == Unit.Type.FIGHTER && u.st.contains(Heartstrings.SpecialTechnology.FORTIFICATION))
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
	
	public int getCapacity(){
		int r = 0;
		for (Unit u: units)
			r += u.getCapacity();
		return r;
	}
	
	public int getMissilesFreeSpace(){
		boolean ttrading = trading;
		if (ttrading)
			endTrade();
		
		int r = 0;
		for (Unit u: units)
			r += u.getMissilesFreeSpace();
		
		if (ttrading)
			beginTrade();
		
		return r;
	}
	
	public int getMissilesAmount(){
		int r = 0;
		for (Unit u: units)
			r += u.missilesLoaded.size();
		return r;
	}
	
	public Missile getMissileAt(int i){
		int j = 0;
		for (Unit u: units)
			for (Missile m: u.missilesLoaded)
				if (i == j++)
					return m;
		return null;
	}
	
	public void loadMissile(Missile m){
		assert(getMissilesFreeSpace() > 0);
		
		units.sort(sortByCapacity);
		
		for (int i = units.size() - 1; i >= 0; --i){
			if (units.get(i).getMissilesFreeSpace() > 0){
				units.get(i).missilesLoaded.add(m);
				return;
			}
		}
		assert(false);
	}
	
	public void unloadMissile(int m){
		unloadMissile(getMissileAt(m));
	}
	public void unloadMissile(Missile tm){
		for (Unit u: units)
			for (Missile m: u.missilesLoaded)
				if (m.equals(tm)){
					u.missilesLoaded.remove(m);
					return;
				}
		assert(false);
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
	@Override
	public void beginTrade(){
		assert(!trading);
		
		//assert(resources.sum() == 0);
		if (trading){ //TODO: Replace with assert and remove DEBUG_INFO field when bug is catched
			System.out.println("ASSERT: Squad.java: prepareForTrading()");
			System.out.println(resources.sum());
			System.out.println(DEBUG_INFO);
			System.out.println("_______________________________________");
		}
		
		for (Unit u: units)
			u.resources.fullFlushTo(resources);
		
		trading = true;
	}

	@Override
	public void endTrade(){
		trading = false;
		
		//Try to distribute resources so maximum amount of missiles can fit
		for (Unit u: units){
			if (resources.sum() == 0)
				return;
			
			if (u.getFreeSpace() > 0){
				for (Resource r: Resource.values())
					if (resources.get(r) > 0){
						int space = u.getFreeSpace();
						while (space >= Missile.WEIGHT)
							space -= Missile.WEIGHT;
						resources.tryTransfer(r, Math.min(space, resources.get(r)), 
						                      u.resources);
					}
			}
		}
		
		//Keep distributing resources if first pass did not succeed
		for (Unit u: units){
			if (resources.sum() == 0)
				return;
			
			if (u.getFreeSpace() > 0){
				for (Resource r: Resource.values())
					if (resources.get(r) > 0)
						resources.tryTransfer(r, Math.min(u.getFreeSpace(), resources.get(r)), 
						                      u.resources);
				}
		}
	}
	
	private void spread(){endTrade();}

	@Override
	public ResourceStorage getTradeStorage() {
		assert(trading);
		return resources;
	}

	@Override
	public boolean isCapacityLimited(){
		return true;
	}

	@Override
	public int getFreeSpace(){
		if (trading){
			return getCapacity() - resources.sum() - getMissilesAmount() * Missile.WEIGHT;
		} else {
			int r = 0;
			for (Unit u: units)
				r += u.getFreeSpace();
			return r;
		}
	}
	////////////////////////////////////////////////////////////////////////////////
	//Menus
	
	private ListEntryCallback LEC_BUILD_MENU = new ListEntryCallback(){
		@Override
		public void action(int id) {
			if (id < Structure.Type.values().length){
				assert(!trading);
				beginTrade();
				boolean x = resources.tryRemove(Resource.METAL, 
				                                Heartstrings.get(Structure.Type.values()[id], 
				                                                 Heartstrings.structureProperties).buildingPrice);
				assert(x);
				endTrade();
				Structure s = new Structure(position, Structure.Type.values()[id], faction);
				faction.registerStructure(s);
				
				Utils.displaceSomewhereWalkable(position, 
				                                Heartstrings.INTERACTION_DISTANCE2 * .2f * .2f, 
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
			boolean buildingAllowed = true, shown = true; 
			if (id < Structure.Type.values().length){
				Structure.Type st = Structure.Type.values()[id];

				
				beginTrade();
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
					if (!faction.isInvestigated(SpecialTechnology.ADVANCED_WARFARE)){
						buildingAllowed = false;
						shown = false;
					} else 
						buildingAllowed = isEnoughMetalForBuilding(Structure.Type.MB);
					
					break;
				case AMD:
					if (!faction.isInvestigated(SpecialTechnology.AMD)){
						buildingAllowed = false;
						shown = false;
					} else
						buildingAllowed  = isEnoughMetalForBuilding(Structure.Type.AMD);
					
					break;
				case ML:
					if (!faction.isInvestigated(SpecialTechnology.STRATEGIC_WARFARE)){
						buildingAllowed = false;
						shown = false;
					} else 
						buildingAllowed = isEnoughMetalForBuilding(Structure.Type.ML);
					
					break;
				case RADAR:
					if (!faction.isInvestigated(SpecialTechnology.RADIO)){
						buildingAllowed = false;
						shown = false;
					} else
						buildingAllowed = isEnoughMetalForBuilding(Structure.Type.RADAR);
					
					break;
				}
				endTrade();
				
				description = Heartstrings.get(st, Heartstrings.structureProperties).description;
				title = Heartstrings.get(st, Heartstrings.structureProperties).title;
			} else
				title = "Cancel";
			
			if (shown)
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
				        interactableStructures.get(id).resources,
				        interactableStructures.get(id).missilesStorage);
			
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
	static abstract class TradeSelectionMenuCallbackPrototype implements ListEntryCallback{
		private ArrayList<Tradeable> nearestTradeables;
		
		public TradeSelectionMenuCallbackPrototype(ArrayList<Tradeable> nt){
			nearestTradeables = nt;
		}
		
		protected abstract void openTradeDialog(Tradeable t);
		
		@Override
		public void action(int id) {
			if (id < nearestTradeables.size())
				openTradeDialog(nearestTradeables.get(id));
			else
				WG.antistatic.uistate = WG.UIState.FREE;
		}

		@Override
		public void entry(Vector2 position, Vector2 size, int id, Color[] color) {
			String title;
			if (id < nearestTradeables.size()){
				Tradeable t = nearestTradeables.get(id);
				t.beginTrade();
				title = nearestTradeables.get(id).getTradeStorage().name;
				t.endTrade();
			} else
				title = "Cancel";
			
			WG.antistatic.gui.advancedButton(position, size, id, this, color, title, null, null);
		}
	};
	private ListEntryCallback LEC_TRADE_SELECTION_MENU = new TradeSelectionMenuCallbackPrototype(potentialTradeables){
		@Override
		protected void openTradeDialog(Tradeable t) {
			trade(t);
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
				me.beginTrade(); ns.beginTrade();
				
				boolean initThumb = false;
				if (!refuelSB.initialized){
					refuelSB.init(position, size, false, GUI.Scrollbar.GUI_SB_DEFAULT_THUMB);
					initThumb = true;
				}
				
				int states = (int)Math.floor(resources.get(Resource.FUEL) + ns.resources.get(Resource.FUEL)) + 1;
				int  leftConstraint = states - 1 - (int)Math.floor(ns.getFreeSpace() + ns.resources.get(Resource.FUEL));
				int rightConstraint = (int)Math.floor(me.getFreeSpace() + me.resources.get(Resource.FUEL));
				
				refuelSB.update(states, leftConstraint, rightConstraint);//update(rightConstraint - leftConstraint + 1)
				if (initThumb)
					refuelSB.offset = (int)Math.floor(me.resources.get(Resource.FUEL));
				//refuelSB.offset = MathUtils.clamp(refuelSB.offset, leftConstraint, rightConstraint);
				
				assert(temporaryFuelTank.sum() == 0);
					me.resources.flushTo(Resource.FUEL, temporaryFuelTank);
					ns.resources.flushTo(Resource.FUEL, temporaryFuelTank);
					assert(temporaryFuelTank.tryTransfer(Resource.FUEL, refuelSB.offset, me.resources));
					temporaryFuelTank.flushTo(Resource.FUEL, ns.resources);
					//Case: Rounding error amount of fuel was flushed to non-transportable squad
					//TODO: Make sure that "me" (left side squad) cannot be receiver from abovementioned case 
					if (ns.resources.get(Resource.FUEL) > ns.getCapacity())
						ns.resources.tryTransfer(Resource.FUEL, 
						                         (int)Math.floor(ns.resources.get(Resource.FUEL) - ns.getCapacity()), 
						                         me.resources);
				assert(temporaryFuelTank.sum() == 0);
				
				refuelSB.render(GUI.GUI_COLORS_SCROLLBAR_COLORS);
				
				me.endTrade(); ns.endTrade();
				
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
			Utils.findStructuresWithinRadius2(interactableStructures, true, faction, position, Heartstrings.INTERACTION_DISTANCE2, null);
			Utils.findTradeablesWithinRadius2(potentialTradeables, true, faction, position, Heartstrings.INTERACTION_DISTANCE2, this);
			
			if (isUnitTypePresent(Unit.Type.BUILDER) &&
			    Utils.isOkToBuild(position))
				piemenu.add(PME_BUILD);

			if (potentialTradeables.size() > 0){
				if (isUnitTypePresent(Unit.Type.TRANSPORTER))
					piemenu.add(PME_TRADE);
			}
			if (interactableStructures.size() > 0){
				
				for (Structure s: interactableStructures)
					if (s.hasYard()){
						piemenu.add(PME_DISBAND);
						break;
					}
			}
			
			Squad ns = Utils.findNearestSquad(faction, position, this);
			if (ns != null &&
			    position.dst2(ns.position) <= Heartstrings.INTERACTION_DISTANCE2 && 
			    (hasFuel() || ns.hasFuel()) && ns.state == State.STAND){
				piemenu.add(PME_REFUEL);
			}
			
			if (canFortify()){
				piemenu.add(PME_FORTIFY);
			}
		}
		if (state == State.FORTIFIED){
			piemenu.add(PME_MOBILIZE);
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
			assert(potentialTradeables.size() > 0);
			
			if (potentialTradeables.size() == 1)
				trade(potentialTradeables.get(0));
			else
				WG.antistatic.setMenu(LEC_TRADE_SELECTION_MENU, potentialTradeables.size() + 1);
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
				        interactableStructures.get(0).resources,
				        interactableStructures.get(0).missilesStorage);
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
	public final PiemenuEntry PME_FORTIFY = new PiemenuEntry("Fortify", new Callback(){
		@Override
		public void action(int source) {
			state = State.FORTIFIED;
		}
	});
	public final PiemenuEntry PME_MOBILIZE = new PiemenuEntry("Mobilize", new Callback(){
		@Override
		public void action(int source) {
			state = State.STAND;
		}
	});
	private void disband(ArrayList<Unit> to, ResourceStorage rs, ArrayList<Missile> ms){
		beginTrade();
		resources.fullFlushTo(rs);
		unloadMissiles(ms);
		to.addAll(units);
		faction.squads.remove(me);
		
	}
	private void trade(Tradeable ta){
		//WG.antistatic.gui.focusedStruct = Utils.findNearestStructure(faction, position, null);
		WG.antistatic.gui.tradeSideA = ta;
		WG.antistatic.gui.tradeSideB = this;
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
