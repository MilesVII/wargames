package com.milesseventh.wargames;

import java.util.ArrayList;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Queue;
import com.milesseventh.wargames.Heartstrings.Craftable;
import com.milesseventh.wargames.Heartstrings.SpecialTechnology;
import com.milesseventh.wargames.Heartstrings.Technology;

public class Structure implements Piemenuable, Combatant, Tradeable{
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
			timeHolder += dt * 1000000f;
			
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
				assert(unitsDone <= amount);
				if (unitsDone == amount)
					break;
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
	public Faction faction;
	public Vector2 position;
	public Type type;
	public ResourceStorage resources;
	public float condition;
	public boolean destroyed = false;

	public ArrayList<Missile> missilesStorage = new ArrayList<Missile>();
	public Missile[] missilesReady = new Missile[Heartstrings.MISSILE_ACTIVE_STORAGE_CAPACITY];
	
	private Queue<CraftingOrder> manufactoryQueue = new Queue<CraftingOrder>();
	private Queue<Unit> repairingQueue = new Queue<Unit>();
	private Queue<Unit> upgradingQueue = new Queue<Unit>();
	
	private ArrayList<Squad> nearbyEnemies = new ArrayList<Squad>();
	private ArrayList<Tradeable> potentialTradeables = new ArrayList<Tradeable>();
	
	public ArrayList<Unit> yard = new ArrayList<Unit>();
	private Random r = new Random();
	
	public Structure(Vector2 npos, Type st, Faction owner) {
		name = "City 17";
		resources = new ResourceStorage(name);
		
		position = npos.cpy();
		faction = owner;
		type = st;
		condition = getMaxCondition();
		
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
		
/*		//Resource Processing
		if (type == Type.CITY)
			processResources(dt);*/
		
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
		
		//Fighting
		if (resources.get(Resource.AMMO) > 0){
			warfareScan(dt);
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
	
	private void warfareScan(float dt){
		nearbyEnemies.clear();
		for (Faction f: Faction.factions){
			if (f == faction)
				continue;
			
			float fightRange2 = Heartstrings.get(type, Heartstrings.structureProperties).fightingRange;
			fightRange2 *= fightRange2;
			Utils.findSquadsWithinRadius2(nearbyEnemies, false, f, position, fightRange2, null);
			
		}
		
		if (nearbyEnemies.size() > 0){
			for (Combatant fucker: nearbyEnemies){
				float bullets = dt / nearbyEnemies.size(); // TODO: Amount of ammo wasted on attack
				bullets = Math.min(bullets, resources.get(Resource.AMMO));
				boolean a = resources.tryRemove(Resource.AMMO, Math.round(bullets * WG.VIRTUAL_FRACTION_SIZE));
				assert(a);
				fucker.receiveFire(bullets * Heartstrings.DEBUG_STRUCTURE_DAMAGE);
			}
		}
	}
	
	public Texture getIcon(){
		if (faction.capital == this)
			return Faction.ICONS[Faction.ICONS.length - 1];
		return Faction.ICONS[type.ordinal()];
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
	
	public boolean hasAnyMissilesInDeployment(){
		for (int i = 0; i < missilesReady.length; ++i)
			if (missilesReady[i] != null)
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
		} while (!WG.antistatic.map.isWalkable(sposition.x, sposition.y) || (ns != null && ns.position.dst2(sposition) < Heartstrings.INTERACTION_DISTANCE2 * .4f));
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
		resources.add(Resource.FUEL, Math.round(Utils.remap(t, 0, 1, k, k * 3) * dt * WG.VIRTUAL_FRACTION_SIZE));
		k = miningHeightToDensity(WG.antistatic.oreMap);
		resources.add(Resource.METAL, Math.round(Utils.remap(t, 0, 1, k, k * 3) * dt * WG.VIRTUAL_FRACTION_SIZE));
	}
	private float miningHeightToDensity(HeightMap map){
		float x = map.getMeta(position.x, position.y);
		if (x < .667f)
			return .2f;
		else
			return Utils.remap(x, .667f, 1, .2f, 1);
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

	public void annexated(Faction f){
		faction.structs.remove(this);
		faction = f;
		faction.structs.add(this);
	}
	
	@Override
	public void receiveFire(float power) {
		condition -= power;
		if (condition <= 0){
			Utils.dropResources(position, resources);
			destroyed = true;
		}
	}
	
	public Missile getMissileForLaunch(){
		return getFirstMountedMissile();
	}
	
	public Missile getFirstMountedMissile(){
		for (Missile m: missilesReady)
			if (m != null && m.isReady())
				return m;
		return null;
	}
	
	public boolean requestMissileLaunch(Vector2 target){
		if (getMissileForLaunch().getFuelNeeded(position, target) <= resources.get(Resource.FUEL)){
			performLaunch(getFirstMountedMissile(), target);
			return true;
		} else
			return false;
	}
	
	public static float getMissileTrajectory(Vector2 from, Vector2 to){
		return from.dst(to) * (float)Math.PI / 2f;
	}
	
	private void performLaunch(Missile m, Vector2 target){
		//Remove missile
		for (int i = 0; i < missilesReady.length; ++i)
			if (missilesReady[i] != null && missilesReady[i].equals(m)){
				missilesReady[i] = null;
				break;
			}
		
		//Remove fuel
		boolean a = resources.tryRemove(Resource.FUEL, m.getFuelNeeded(position, target));
		assert(a);
		
		//Load and register missile
		m.setLaunchData(position, target, true);
		Faction.missilesInAir.add(m);
	}
	
	@Override
	public Vector2 getPosition(){
		return position;
	}
	
	public void repair(float repair){
		condition += repair;
		condition = MathUtils.clamp(condition, 0, getMaxCondition());
	}
	
	public float getMaxCondition(){
		return Heartstrings.get(type, Heartstrings.structureProperties).maxCondition;
	}
	
	@Override
	public void beginTrade() {}
	@Override
	public ResourceStorage getTradeStorage() {
		return resources;
	}
	@Override
	public void endTrade() {}

	@Override
	public boolean isCapacityLimited() {
		return false;
	}

	@Override
	public int getFreeSpace() {
		assert(false);
		return Integer.MAX_VALUE;
	}
	////////////////////////////////////////////////////////////////////////////////
	//Pie Menus
	
	public final ArrayList<PiemenuEntry> piemenu = new ArrayList<PiemenuEntry>();
	private void rebuildPiemenu(){
		piemenu.clear();
		piemenu.add(PiemenuEntry.PME_CANCEL);
		piemenu.add(PME_STATS);
		if (type == Type.CITY && faction.capital == this)
			piemenu.add(PME_LAB);
		if (type == Type.CITY || type == Type.MB)
			piemenu.add(PME_CRAFT);
		if ((!yard.isEmpty() && hasYard()) || isCraftingInProcess())
			piemenu.add(PME_YARD);
		Squad ns = Utils.findNearestSquad(faction, position, null);
		Utils.findTradeablesWithinRadius2(potentialTradeables, true, faction, position, Heartstrings.INTERACTION_DISTANCE2, this);

		if (ns != null && ns.position.dst2(position) < Heartstrings.INTERACTION_DISTANCE2 &&
		    ns.state == Squad.State.STAND &&
		    (type == Type.CITY || type == Type.MB || type == Type.ML) &&
		    faction.isInvestigated(SpecialTechnology.STRATEGIC_WARFARE)){
			piemenu.add(PME_MISSILE_TRADE);
		}
		if (potentialTradeables.size() > 0)
			piemenu.add(PME_TRADE);
		if (type == Type.ML && (!missilesStorage.isEmpty() || hasAnyMissilesInDeployment()))
			piemenu.add(PME_MISSILE_DEPLOY);
		if (type == Type.ML && getFirstMountedMissile() != null)
			piemenu.add(PME_MISSILE_LAUNCH);
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
	public final PiemenuEntry PME_MISSILE_LAUNCH = new PiemenuEntry("Missile Launch", new Callback(){
		@Override
		public void action(int source) {
			WG.antistatic.uistate = WG.UIState.MISSILE_LAUNCH;
		}
	});
	private ListEntryCallback LEC_TRADE_SELECTION_MENU = new Squad.TradeSelectionMenuCallbackPrototype(potentialTradeables){
		@Override
		protected void openTradeDialog(Tradeable t) {
			trade(t);
		}
	};
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
	private void trade(Tradeable ta){
		WG.antistatic.gui.tradeSideA = this;
		WG.antistatic.gui.tradeSideB = ta;
		WG.antistatic.openDialog(WG.Dialog.TRADE);
	}
	
	//Piemenuable interface implementation
	@Override
	public Vector2 getWorldPosition() {
		return position;
	}
	@Override
	public ArrayList<PiemenuEntry> getEntries() {
		return piemenu;
	}
}
