package com.milesseventh.wargames;

import java.util.ArrayList;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Queue;
import com.milesseventh.wargames.Heartstrings.Craftable;
import com.milesseventh.wargames.Heartstrings.SpecialTechnology;
import com.milesseventh.wargames.Heartstrings.Technology;

public class Structure implements Piemenuable{
	//////////////////////////////////////////////////////////////////
	//STRUCTURE TYPES:
	//> City: WLUCT
	//> Miner: WL
	//> Missile Launcher: WLM
	//> Radar: none
	//> AMD: WL
	//> Military Base: WLUC
	//COMPONENTS OF STRUCTURE:
	//OK W = Warehouse (no own dialog) > Turned into Stats
	//OK L = Column loading > Implemented as Trading
	//OK U = Units building (Cities and MB's only)
	//OK C = Column management (Cities only) > Implemented as Deployment
	//OK T = Lab (Capital city only)
	//> M = Missile launch
	
	public class CraftingOrder {
		private Craftable craftable;
		private int amount, unitsDone = 0;
		private float[] tech;
		private ArrayList<SpecialTechnology> st;
		private float singleCraftTime, timeHolder;
		private Structure manufacturer;
		public float currentUnitProgress = 0;
		public int unitsToGo = -1;
		
		@SuppressWarnings("unchecked")
		public CraftingOrder(Structure nmanufacturer, Craftable ncraftable, int namount, float[] nt, ArrayList<SpecialTechnology> nst, float nsingleCraftTime){
			manufacturer = nmanufacturer;
			craftable = ncraftable;
			amount = namount;
			tech = nt.clone();
			st = (ArrayList<SpecialTechnology>)nst.clone();
			singleCraftTime = nsingleCraftTime;
		}
		
		public boolean craft(float dt){
			timeHolder += dt;
			
			for (int i = 0; i < Math.floor(timeHolder / singleCraftTime); ++i){
				switch(craftable){
				case AMMO:
					resources.add(Resource.AMMO, 1);
					break;
				case MISSILE:
					missilesStorage.add(new Missile(tech, st));
					break;
				case SCIENCE:
					++Faction.debug.scienceDataAvailable;
					break;
				case TRANSPORTER:
					yard.add(new Unit(manufacturer, Unit.Type.TRANSPORTER, tech, st));
					break;
				case BUILDER:
					yard.add(new Unit(manufacturer, Unit.Type.BUILDER, tech, st));
					break;
				case FIGHTER:
					yard.add(new Unit(manufacturer, Unit.Type.FIGHTER, tech, st));
					break;
				}
				
				timeHolder -= singleCraftTime;
				++unitsDone;
			}
			
			currentUnitProgress = timeHolder / singleCraftTime;
			unitsToGo = amount - unitsDone;
			
			return unitsDone >= amount;
		}
		
	}
	public class UpgradeOrder {
		public Unit unit;
		public float time;
		
		public UpgradeOrder(Unit nu, float nt){
			unit = nu;
			time = nt;
		}
	}
	
	public enum Type{
		CITY, MINER, MB, ML, RADAR, AMD 
	}
	
	public String name;
	private float range;//Radius of circle that will be added to faction's territory
	public Faction faction;//ID of faction that owns this unit
	private float vitality, maxVitality;
	public Vector2 position;
	public Type type;
	public ResourceStorage resources;

	public ArrayList<Missile> missilesStorage = new ArrayList<Missile>();
	public Missile[] missilesReady = new Missile[Heartstrings.MISSILE_ACTIVE_STORAGE_CAPACITY];
	
	private Queue<CraftingOrder> manufactoryQueue = new Queue<CraftingOrder>();
	private Queue<Unit> repairingQueue = new Queue<Unit>();
	private Queue<Unit> upgradingQueue = new Queue<Unit>();
	
	public ArrayList<Unit> yard = new ArrayList<Unit>();
	private Random r = new Random();
	
	public Structure(Vector2 npos, Type st, Faction owner) {
		name = "City 17";
		resources = new ResourceStorage(name);
		
		position = npos.cpy();
		faction = owner;
		type = st;
		//range = Heartstrings.get
		vitality = maxVitality = 1;//Heartstrings.get
		
		rebuildPiemenu();
	}
	
	public void orderCrafting(Craftable c, int amount, float[] t, ArrayList<SpecialTechnology> st){
		Resource[] ingridients = Heartstrings.get(c, Heartstrings.craftableProperties).ingridients;
		for (int i = 0; i < ingridients.length; ++i)
			if (!resources.tryRemove(ingridients[i], Heartstrings.getCraftingCost(c, ingridients[i], t, st, amount)))
				System.err.println("Bankrupt occured while withdrawing crafting order price");
		
		float sct = Heartstrings.getWorkamount(c, t, st) / getCraftSpeed();
		
		manufactoryQueue.addLast(new CraftingOrder(this, c, amount, t, st, sct));
	}
	
	public void repairUnits(float dt){
		if (repairingQueue.size > 0){
			float repairAvailable = getCraftSpeed() * dt;
			
			do {
				Unit u = repairingQueue.first();
				if (u.getMaxCondition() - u.condition > repairAvailable){
					u.condition += repairAvailable;
					repairAvailable = -1;
				} else {
					repairAvailable -= u.getMaxCondition() - u.condition;
					u.condition = u.getMaxCondition();
					u.state = Unit.State.PARKED;
					repairingQueue.removeFirst();
				}
			} while(repairAvailable > 0 && repairingQueue.size > 0);
		}
	}
	
	public void orderRepairing(Unit u){
		assert(yard.contains(u));
		assert(u.canBeRepaired(this));
		resources.tryRemove(Resource.METAL, Math.round(Heartstrings.getRepairCostInMetal(u)));
		repairingQueue.addLast(u);
		u.state = Unit.State.REPAIRING;
	}
	
	private static final float REPAIR_CANCELATION_REFUND = .8f;
	public void cancelRepairing(Unit u){
		assert(yard.contains(u));
		assert(u.state == Unit.State.REPAIRING);
		u.state = Unit.State.PARKED;
		repairingQueue.removeValue(u, false);
		
		resources.add(Resource.METAL, (int)Math.floor(Heartstrings.getRepairCostInMetal(u) * REPAIR_CANCELATION_REFUND)); //refund
	}
	
	public void orderUprgade(Unit u, float[] nt, ArrayList<SpecialTechnology> stToAdd){
		assert(yard.contains(u));
		resources.tryRemove(Resource.METAL, Math.round(Heartstrings.getUpgradeCostInMetal(u, nt, stToAdd)));
		u.st.addAll(stToAdd);
		u.upgradeTime = Heartstrings.getUpgradeWorkamount(u, nt, stToAdd) / getCraftSpeed();
		u.techLevel = nt;
		u.condition = u.getMaxCondition(); //In case armor was upgraded
		u.state = Unit.State.UPGRADING;
		upgradingQueue.addLast(u);
	}
	
	public void update(float dt){
		//Craft
		if (manufactoryQueue.size > 0)
			if (manufactoryQueue.first().craft(dt))
				manufactoryQueue.removeFirst();
		
		//Repairing
		repairUnits(dt);
		
		//Upgrading
		if (upgradingQueue.size > 0)
			if (upgradingQueue.first().upgradeTime < dt){
				upgradingQueue.first().state = Unit.State.PARKED;
				upgradingQueue.removeFirst();
			} else
				upgradingQueue.first().upgradeTime -= dt;
		
		//Mining
		if (type == Type.MINER)
			mine(dt);
		
		//Resource Processing
		if (type == Type.CITY)
			processResources(dt);
		
		//Missile mounting
		for (int i = 0; i < missilesReady.length; ++i){
			Missile m = missilesReady[i];
			
			if (m == null)
				continue;
			//if (!m.isReady() && !m.isUnmounted())
			m.executeOrder(getMissileMountingSpeed() * dt);
			
			if (m.isUnmounted()){
				missilesStorage.add(m);
				missilesReady[i] = null;
			}
		}
		
		//Interaction
		if (WG.antistatic.getUIFromWorldV(position).dst(Utils.UIMousePosition) < WG.STRUCTURE_ICON_RADIUS * 1.2f){
			if (Gdx.input.justTouched()){
				WG.antistatic.setFocusOnPiemenuable(this);
			}
			
			WG.antistatic.gui.prompt(name);
		}
		rebuildPiemenu();
	}
	
	public Texture getIcon(){
		if (faction.capital == this)
			return Faction.ICONS[Faction.ICONS.length - 1];
		return Faction.ICONS[type.ordinal()];
	}
	
	protected void onDestroy() {
		faction.unregisterStructure(this);
	}
	
	public float getRange(){
		return range;
	}
	
	private static final float MIN_CRFTSP_K = .8f, MAX_CRFTSP_K = 1.7f;
	public float getCraftSpeed(){
		float cs = Heartstrings.get(type, Heartstrings.structureProperties).craftSpeed;
		
		return Utils.remap(faction.techLevel(Technology.ENGINEERING), 0, 1, 
		                   cs * MIN_CRFTSP_K, cs * MAX_CRFTSP_K) * 10f;
	}
	
	public void orderMissileMounting(Missile m){
		assert(missilesStorage.contains(m));
		assert(hasEmptySlotsForMissiles());
		
		int i;
		for (i = 0; missilesReady[i] != null; ++i){}
		
		missilesReady[i] = m;
		missilesStorage.remove(m);
		
		m.orderedState = Missile.State.READY;
	}
	
	public float getMissileMountingSpeed(){
		return Utils.remap(faction.techLevel(Technology.ENGINEERING), 0, 1, 
		                   Heartstrings.MISSILE_MOUNTING_SPEED_MIN, 
		                   Heartstrings.MISSILE_MOUNTING_SPEED_MAX);
	}
	
	public boolean hasEmptySlotsForMissiles(){
		for (int i = 0; i < missilesReady.length; ++i)
			if (missilesReady[i] == null)
				return true;
		return false;
	}
	
	public boolean hasYard(){
		return type == Type.CITY || type == Type.MB;
	}
	
	@SuppressWarnings("unchecked")
	public void deploySquad(ArrayList<Unit> units){
		if (units.isEmpty() || yard.isEmpty())
			return;
		
		Vector2 sposition;
		Squad ns;
		int j = 0;
		do {
			sposition = Utils.getVector(position).add(Utils.getVector(WG.STRUCTURE_DEPLOYMENT_SPREAD_MIN, 0).rotate(r.nextInt(360)));
			if (++j > 360){
				System.out.println("E: Nowhere to deploy");
				return;
			}
			ns = Utils.findNearestSquad(faction, sposition, null);
		} while (!WG.antistatic.map.isWalkable(sposition.x, sposition.y) || (ns != null && ns.position.dst2(sposition) < Heartstrings.STRUCTURE_INTERACTION_DISTANCE2 * .4f));
		Squad s = new Squad(faction, sposition);
		
		for (Unit u: (ArrayList<Unit>)units.clone()){
			assert(yard.contains(u));
			if (yard.contains(u)){
				yard.remove(u);
				s.units.add(u);
			} else {
				System.out.println("E: Malformed deployment list");
			}
		}
		
		if (!s.units.isEmpty())
			faction.squads.add(s);
	}
	
	private void mine(float dt){
		float t = faction.techLevel(Technology.ENGINEERING);
		
		float k = miningHeightToDensity(WG.antistatic.oilMap);
		resources.add(Resource.OIL, Utils.remap(t, 0, 1, k, k * 3) * dt);
		k = miningHeightToDensity(WG.antistatic.oreMap);
		resources.add(Resource.ORE, Utils.remap(t, 0, 1, k, k * 3) * dt);
	}
	private float miningHeightToDensity(HeightMap map){
		float x = map.getMeta(position.x, position.y);
		if (x < .667f)
			return .2f;
		else
			return Utils.remap(x, .667f, 1, .2f, 1);
	}

	private static final float ORE_TO_METAL_PROCESS_RATIO_MIN = 3f,
	                           ORE_TO_METAL_PROCESS_RATIO_MAX = 1.2f,
	                           OIL_TO_FUEL_PROCESS_RATIO_MIN = 4f,
	                           OIL_TO_FUEL_PROCESS_RATIO_MAX = 1.7f,
	                           RESOURCE_PROCESSING_RATE_MIN = .7f,
	                           RESOURCE_PROCESSING_RATE_MAX = 7f; //Per second
	private void processResources(float dt){
		float t = faction.techLevel(Technology.ENGINEERING);
		float rate = Utils.remap(t, 0, 1, RESOURCE_PROCESSING_RATE_MIN, RESOURCE_PROCESSING_RATE_MAX);
		float ratio = Utils.remap(t, 0, 1, ORE_TO_METAL_PROCESS_RATIO_MIN, ORE_TO_METAL_PROCESS_RATIO_MAX);
		float r = resources.get(Resource.ORE);
		float amount = Math.min(rate * dt, r);
		boolean x = resources.tryRemove(Resource.ORE, amount);
		assert(x);
		resources.add(Resource.METAL, amount / ratio);
		
		ratio = Utils.remap(t, 0, 1, OIL_TO_FUEL_PROCESS_RATIO_MIN, OIL_TO_FUEL_PROCESS_RATIO_MAX);
		r = resources.get(Resource.OIL);
		amount = Math.min(rate * dt, r);
		x = resources.tryRemove(Resource.OIL, amount);
		assert(x);
		resources.add(Resource.FUEL, amount / ratio);
	}

	public boolean isCraftingInProcess(){
		return manufactoryQueue.size > 0;
	}
	
	public int unitsCrafting(){
		assert(manufactoryQueue.size > 0);
		
		int r = manufactoryQueue.first().unitsToGo;
		for (int i = 1; i < manufactoryQueue.size; i++)
			r += manufactoryQueue.get(i).amount;
		return r;
	}
	
	public float unitCraftingProgress(){
		assert(manufactoryQueue.size > 0);
		return manufactoryQueue.first().currentUnitProgress;
	}
	
	public void hit(float _damage){
		vitality -= _damage;
		if (vitality <= 0){
			onDestroy();
		}
	}
	
	public void repair(float _repair){
		vitality += _repair;
		if (vitality > maxVitality){
			vitality = maxVitality;
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////
	//Pie Menus
	
	public final ArrayList<PiemenuEntry> PIEMENU = new ArrayList<PiemenuEntry>();
	private void rebuildPiemenu(){
		PIEMENU.clear();
		PIEMENU.add(PiemenuEntry.PME_CANCEL);
		PIEMENU.add(PME_STATS);
		if (type == Type.CITY && faction.capital == this)
			PIEMENU.add(PME_LAB);
		if (type == Type.CITY || type == Type.MB)
			PIEMENU.add(PME_CRAFT);
		if ((!yard.isEmpty() && hasYard()) || isCraftingInProcess())
			PIEMENU.add(PME_YARD);
		Squad ns = Utils.findNearestSquad(faction, position, null);
		if (ns != null && ns.position.dst2(position) < Heartstrings.STRUCTURE_INTERACTION_DISTANCE2 &&
		    ns.state == Squad.State.STAND){
			PIEMENU.add(PME_MISSILE_TRADE);
		}
		if (type == Type.ML)
			PIEMENU.add(PME_MISSILE_DEPLOY);
	}
	
	public final PiemenuEntry PME_STATS = new PiemenuEntry("Stats", new Callback(){
		@Override
		public void action(int source) {
			WG.antistatic.openDialog(WG.Dialog.STATS);
		}
	});
	public final PiemenuEntry PME_LAB = new PiemenuEntry("R&D", new Callback(){
		@Override
		public void action(int source) {
			WG.antistatic.openDialog(WG.Dialog.LABORATORY);
		}
	});
	public final PiemenuEntry PME_CRAFT = new PiemenuEntry("Craft", new Callback(){
		@Override
		public void action(int source) {
			WG.antistatic.openDialog(WG.Dialog.CRAFTING);
		}
	});
	public final PiemenuEntry PME_YARD = new PiemenuEntry("Deploy", new Callback(){
		@Override
		public void action(int source) {
			WG.antistatic.openDialog(WG.Dialog.YARD);
		}
	});
	public final PiemenuEntry PME_MISSILE_TRADE = new PiemenuEntry("Missile Exchange", new Callback(){
		@Override
		public void action(int source) {
			Squad ns = Utils.findNearestSquad(faction, position, null);
			assert(ns != null);
			WG.antistatic.gui.focusedSquad = ns;
			WG.antistatic.openDialog(WG.Dialog.MISSILE_EXCHANGE);
		}
	});
	public final PiemenuEntry PME_MISSILE_DEPLOY = new PiemenuEntry("Missile Deploying", new Callback(){
		@Override
		public void action(int source) {
			WG.antistatic.openDialog(WG.Dialog.MISSILE_DEPLOY);
		}
	});
	
	//Piemenuable interface implementation
	@Override
	public Vector2 getWorldPosition() {
		return position;
	}
	@Override
	public ArrayList<PiemenuEntry> getEntries() {
		return PIEMENU;
	}
}
