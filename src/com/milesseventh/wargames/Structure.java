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
		
		public CraftingOrder(Structure nmanufacturer, Craftable ncraftable, int namount, float[] nt, ArrayList<SpecialTechnology> nst, float nsingleCraftTime){
			manufacturer = nmanufacturer;
			craftable = ncraftable;
			amount = namount;
			tech = nt;
			st = nst;
			singleCraftTime = nsingleCraftTime;
		}
		
		public boolean craft(float dt){
			//done += manufacturer.getCraftSpeed() * dt;
			//done = Math.min(workamount * (float)amount, done);
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
		assert(resources.tryRemove(Resource.METAL, Math.round(Heartstrings.getRepairCostInMetal(u))));
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
		assert(resources.tryRemove(Resource.METAL, Math.round(Heartstrings.getUpgradeCostInMetal(u, nt, stToAdd))));
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
	
	@SuppressWarnings("unchecked")
	public void deploySquad(ArrayList<Unit> units){
		if (units.isEmpty() || yard.isEmpty())
			return;
		
		Vector2 sposition;
		int j = 0;
		do {
			sposition = Utils.getVector(position).add(Utils.getVector(WG.STRUCTURE_DEPLOYMENT_SPREAD_MIN, 0).rotate(r.nextInt(360)));
			if (++j > 360){
				System.out.println("E: Nowhere to deploy");
				return;
			}
		} while (!WG.antistatic.map.isWalkable(sposition.x, sposition.y));
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
		if (!yard.isEmpty() && (type == Type.CITY || type == Type.MB))
			PIEMENU.add(PME_YARD);
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
