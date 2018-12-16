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
	//> City: WLUCIT;           Universal sructure. Medium defence
	//> Miner: WL               Ore miner. Improved resource loader, no defence
	//> Missile Launcher: WLM   Missile silo. Weak defence, has weak radar
	//> Radar: none             Radar. Controls missile's flight, weak defence
	//> AMD: WL                 Anti-Missile Defence. Weak defence, can shoot down incoming missiles.
	//> Military Base: WLUC     Military forification. Can build own units, strong defence
	//COMPONENTS OF STRUCTURE:
	//> W = Warehouse (no own dialog)
	//> L = Column loading
	//> U = Units building (Cities and MB's only)
	//> C = Column management (Cities only)
	//> T = Lab (Capital city only)
	//> I = Industrial management (Cities only)
	//> M = Missile launch
	//CRAFTABLES
	//Units: Fighters, Transporters, Builders: Metal
	//Missiles,Ammo: Metal, fuel
	//Science data: Metal, Ammo
	
	public class CraftingOrder {
		Craftable craftable;
		int amount, unitsDone = 0;
		float[] tech;
		ArrayList<SpecialTechnology> st;
		float workamount, done, wholeWA;
		Structure manufacturer;
		
		public CraftingOrder(Structure nmanufacturer, Craftable ncraftable, int namount, float[] nt, ArrayList<SpecialTechnology> nst, float nworkamount){
			manufacturer = nmanufacturer;
			craftable = ncraftable;
			amount = namount;
			tech = nt;
			st = nst;
			workamount = nworkamount;
			done = 0f;
		}
		
		public boolean craft(float dt){
			done += manufacturer.getCraftSpeed() * dt;
			done = Math.min(workamount * (float)amount, done);
			
			int nunitsDone = (int)Math.ceil(done / workamount);
			for (int i = 0; i < nunitsDone; ++i){
				switch(craftable){
				case AMMO:
					addResource(Resource.AMMO, 1);
					break;
				case MISSILE:
					addResource(Resource.MISSILE, 1);
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
			}
			unitsDone += nunitsDone;
			
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
	private float[] resources = {0, 0, 0, 0, 0, 0};
	public enum Resource {
		ORE, METAL, AMMO, MISSILE, OIL, FUEL
	}
	
	public enum Type{
		CITY, MINER, MB, ML, RADAR, AMD 
	}
	
	private float range;//Radius of circle that will be added to faction's territory
	public Faction ownerFaction;//ID of faction that owns this unit
	private float vitality, maxVitality;
	public Vector2 position;
	public Type type;
	
	private Queue<CraftingOrder> manufactoryQueue = new Queue<CraftingOrder>();
	private Queue<Unit> repairingQueue = new Queue<Unit>();
	private Queue<Unit> upgradingQueue = new Queue<Unit>();
	
	public ArrayList<Unit> yard = new ArrayList<Unit>();
	private Random r = new Random();
	
	public final ArrayList<PiemenuEntry> PIEMENU = new ArrayList<PiemenuEntry>();
	private void rebuildPiemenu(){
		PIEMENU.clear();
		PIEMENU.add(PiemenuEntry.PME_CANCEL);
		PIEMENU.add(PME_STATS);
		PIEMENU.add(PME_LAB);
		PIEMENU.add(PME_CRAFT);
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
	
	public Structure(Vector2 npos, Type st, Faction owner) {
		position = npos.cpy();
		ownerFaction = owner;
		type = st;
		//range = Heartstrings.get
		vitality = maxVitality = 1;//Heartstrings.get
		
		rebuildPiemenu();
	}
	
	public float getResource(Resource _resType){
		return resources[_resType.ordinal()];
	}
	
	public void addResource(Resource _resType, float _add){
		resources[_resType.ordinal()] += _add;
	}
	
	public boolean tryRemoveResource(Resource _resType, float _subtr){
		if (resources[_resType.ordinal()] >= _subtr){
			resources[_resType.ordinal()] -= _subtr;
			return true;
		} else
			return false;
	}
	
	public void orderCrafting(Craftable c, int amount, float[] t, ArrayList<SpecialTechnology> st){
		Resource[] ingridients = Heartstrings.get(c, Heartstrings.craftableProperties).ingridients;
		for (int i = 0; i < ingridients.length; ++i)
			if (!tryRemoveResource(ingridients[i], Heartstrings.getCraftingCost(c, ingridients[i], t, st, amount)))
				System.err.println("Bankrupt occured while withdrawing crafting order price");
		
		float wa = Heartstrings.getWorkamount(c, t, st);
		
		manufactoryQueue.addLast(new CraftingOrder(this, c, amount, t, st, wa));
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
		assert(this.tryRemoveResource(Resource.METAL, Heartstrings.getRepairCostInMetal(u)));
		repairingQueue.addLast(u);
		u.state = Unit.State.REPAIRING;
	}
	
	private static final float REPAIR_CANCELATION_REFUND = .8f;
	public void cancelRepairing(Unit u){
		assert(yard.contains(u));
		assert(u.state == Unit.State.REPAIRING);
		u.state = Unit.State.PARKED;
		repairingQueue.removeValue(u, false);
		
		addResource(Resource.METAL, Heartstrings.getRepairCostInMetal(u) * REPAIR_CANCELATION_REFUND); //refund
	}
	
	public void orderUprgade(Unit u, float[] nt, ArrayList<SpecialTechnology> stToAdd){
		assert(yard.contains(u));
		assert(this.tryRemoveResource(Resource.METAL, Heartstrings.getUpgradeCostInMetal(u, nt, stToAdd)));
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
			if(manufactoryQueue.first().craft(dt))
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
		}
	}
	
	public Texture getIcon(){
		if (ownerFaction.capital == this)
			return Faction.ICONS[Faction.ICONS.length - 1];
		return Faction.ICONS[type.ordinal()];
	}
	
	protected void onDestroy() {
		ownerFaction.unregisterStructure(this);
	}
	
	public float getRange(){
		return range;
	}
	
	private static final float MIN_CRFTSP_K = .8f, MAX_CRFTSP_K = 1.7f;
	public float getCraftSpeed(){
		float cs = Heartstrings.get(type, Heartstrings.structureProperties).craftSpeed;
		
		return Utils.remap(ownerFaction.techLevel(Technology.ENGINEERING), 0, 1, 
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
		Squad s = new Squad(ownerFaction, sposition);
		
		for (Unit u: (ArrayList<Unit>)units.clone()){
			assert(!yard.contains(u));
			if (yard.contains(u)){
				yard.remove(u);
				s.units.add(u);
			} else {
				System.out.println("E: Malformed deployment list");
			}
		}
		
		if (!s.units.isEmpty())
			ownerFaction.squads.add(s);
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
