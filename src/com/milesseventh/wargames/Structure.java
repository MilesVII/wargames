package com.milesseventh.wargames;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Queue;
import com.milesseventh.wargames.Heartstrings.Craftable;
import com.milesseventh.wargames.Heartstrings.SpecialTechnology;
import com.milesseventh.wargames.Heartstrings.Technology;
import com.milesseventh.wargames.WG.Dialog;

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
			done += DEFAULT_CRAFTING_PER_MS * dt * MAX_CRAFTING_SPEED_DISCOUNT * (evolution / (float)MAX_EVOLUTION);
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
					yard.add(new Unit(manufacturer, Unit.Type.TRANSPORTER, tech));
					break;
				case BUILDER:
					yard.add(new Unit(manufacturer, Unit.Type.BUILDER, tech));
					break;
				case FIGHTER:
					yard.add(new Unit(manufacturer, Unit.Type.FIGHTER, tech));
					break;
				}
			}
			unitsDone += nunitsDone;
			
			return unitsDone >= amount;
		}
		
	}
	
	private float[] resources = {0, 0, 0, 0, 0, 0};
	public enum Resource {
		ORE, METAL, AMMO, MISSILE, OIL, FUEL
	}
	
	public enum StructureType{
		CITY, MINER, ML, RADAR, AMD, MB 
	}
	//                                             C   M   ML   R  AMD   MB
	public static final float[] DEFAULT_RANGES = { 22,  0, 17, 12,  27,  42};//Firing range
	public static final float[] DEFAULT_MAXCDS = {420, 70, 42, 70, 120, 200};//Max vitality
	public static final int[]   PIEMENU_ACTCNT = {  5,  0,  0,  0,   0,   0};//Pie menu actions amount

	
	
	public static final float MAX_CRAFTING_RESOURCE_DISCOUNT = .32f;
	public static final float MAX_CRAFTING_SPEED_DISCOUNT = .7f;
	public static final float DEFAULT_CRAFTING_PER_MS = .7f;
	public static final int EVOLUTION_PER_UNIT_CRAFTED = 2;
	public static final int EVOLUTION_PER_SQUAD_DESTROYED = 3;
	public static final int EVOLUTION_PER_RESOURCE_CONVERTED = 2;
	public static final int MAX_EVOLUTION = 70000;
	public int evolution = 0; //Evolution factor defines the speed of crafting and firepower of defence systems
	
	private float range;//Radius of circle that will be added to faction's territory
	public Faction ownerFaction;//ID of faction that owns this unit
	private float vitality, maxVitality;
	public Vector2 position;
	public StructureType type;
	private Queue<CraftingOrder> manufactoryQueue = new Queue<CraftingOrder>();
	public ArrayList<Unit> yard = new ArrayList<Unit>();
	
	public static final Callback PIEMENU_ACTIONS_CITY = new Callback(){
		@Override
		public void action(int source) {
			switch(source){
			case(0):
				//System.out.println("Cancelled");
				break;
			case(1):
				WG.antistatic.currentDialog = WG.Dialog.STATS;
				break;
			case(2):
				WG.antistatic.currentDialog = WG.Dialog.LABORATORY;
				break;
			case(3):
				WG.antistatic.currentDialog = WG.Dialog.CRAFTING;
				break;
			case(4):
				//DEPLOY
				break;
			}
		}
	};
	public static final String[] PIEMENU_CAPTIONS_CITY = {
		"Cancel", "Stats", "R&D", "Craft", "Deploy"
	};
	public Structure(Vector2 npos, StructureType st, Faction owner) {
		position = npos.cpy();
		ownerFaction = owner;
		type = st;
		range = DEFAULT_RANGES[type.ordinal()];//Assign firing range and condition on the assumption of type
		vitality = maxVitality = DEFAULT_MAXCDS[type.ordinal()];
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
	
	public float transfer(Resource _resType, float trans){
		float transaction = Math.min(trans, resources[_resType.ordinal()]);
		resources[_resType.ordinal()] -= transaction;
		return transaction;
	}
	
	public float getCraftingBonus(){
		return MAX_CRAFTING_RESOURCE_DISCOUNT * (evolution / (float)MAX_EVOLUTION);
	}
	
	public float getCraftingBonus(int unitsCrafted){
		return MAX_CRAFTING_RESOURCE_DISCOUNT * Math.min(1f, (evolution + EVOLUTION_PER_UNIT_CRAFTED * unitsCrafted) / (float)MAX_EVOLUTION);
	}
	
	public void orderCrafting(Craftable c, int amount, float[] t, ArrayList<SpecialTechnology> st){
		Resource[] ingridients = Heartstrings.get(c, Heartstrings.craftableProperties).ingridients;
		for (int i = 0; i < ingridients.length; ++i)
			if (!tryRemoveResource(ingridients[i], Heartstrings.getCraftingCost(c, ingridients[i], this, t, st, amount)))
				System.err.println("Bankrupt occured while withdrawing crafting order price");
		
		float wa = Heartstrings.get(c, Heartstrings.craftableProperties).workamount;
		for (int i = 0; i < Technology.values().length; ++i)
			wa += Heartstrings.tProperties[i].maxMarkup * t[i];
		for (SpecialTechnology i : st)
			wa += Heartstrings.get(i, Heartstrings.stProperties).workamountMarkup;
		
		manufactoryQueue.addLast(new CraftingOrder(this, c, amount, t, st, wa));
	}
	
	public void update(float dt){
		//Craft
		if (manufactoryQueue.size > 0)
			if(manufactoryQueue.first().craft(dt))
				manufactoryQueue.removeFirst();
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
	
	public void update(){
		if (WG.antistatic.getUIFromWorldV(position).dst(Utils.UIMousePosition) < WG.STRUCTURE_ICON_RADIUS * 1.2f && WG.antistatic.currentDialog == Dialog.NONE){
			if (Gdx.input.justTouched()){
				WG.antistatic.setFocusOnPiemenuable(this);
			}
		}
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
	
	public static int getPieMenuActionsNumber(StructureType st){
		return PIEMENU_ACTCNT[st.ordinal()];
	}
	
	//Piemenuable interface implementation
	@Override
	public Vector2 getWorldPosition() {
		return position;
	}

	@Override
	public int getActionsAmount() {
		return PIEMENU_ACTCNT[type.ordinal()];
	}

	@Override
	public Callback getAction() {
		// TODO Auto-generated method stub
		return PIEMENU_ACTIONS_CITY;
	}
}
