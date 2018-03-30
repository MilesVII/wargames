package com.milesseventh.wargames;

import java.util.ArrayList;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Queue;
import com.milesseventh.wargames.Heartstrings.Craftable;
import com.milesseventh.wargames.Heartstrings.SpecialTechnology;

public class Structure{
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
		public CraftingOrder(Craftable ncraftable, int namount, float[] nt, ArrayList<SpecialTechnology> nst, float nworkamount){
			craftable = ncraftable;
			amount = namount;
			tech = nt;
			st = nst;
			workamount = nworkamount;
			done = 0f;
		}
		
		public boolean craft(float dt){
			return false;//TODO: stoped here
		}
		
		Craftable craftable;
		int amount;
		float[] tech;
		ArrayList<SpecialTechnology> st;
		float workamount, done;
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
	public static final int[]   PIEMENU_ACTCNT = {  5,  0,  0,  0,   0,   0};//Pie-menu actions counter
	
	private float range;//Radius of circle that will be added to fraction's territory
	public Fraction ownerFraction;//ID of fraction that owns this unit
	private float vitality, maxVitality;
	private Vector2 position;
	public StructureType type;
	public int unitsCrafted = 0;//
	public float evolution = 0; //Evolution factor defines the speed of crafting and firepower of defence systems
	private Queue<CraftingOrder> manufactoryQueue = new Queue<CraftingOrder>();
	
	public static final Callback PIEMENU_ACTIONS_CITY = new Callback(){
		@Override
		public void action(int source) {
			switch(source){
			case(0):
				System.out.println("Cancelled");
				break;
			case(1):
				WG.antistatic.currentDialog = WG.Dialog.LABORATORY;
				break;
			case(2):
				WG.antistatic.currentDialog = WG.Dialog.CRAFTING;
				break;
			}
		}
	};
	
	public Structure(Vector2 pos, StructureType st, Fraction owner) {
		position = pos;
		ownerFraction = owner;
		type = st;
		range = DEFAULT_RANGES[type.ordinal()];//Assign firing range and condition on the assumption of type
		vitality = maxVitality = DEFAULT_MAXCDS[type.ordinal()];
	}
	
	public static int getPieMenuActionsNumber(StructureType st){
		return PIEMENU_ACTCNT[st.ordinal()];
	}
	
	public int getPieMenuActionsNumber(){
		return PIEMENU_ACTCNT[type.ordinal()];
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
	
	public static final int MAX_UNITS_FOR_DISCOUNT = 1200;// Number of units being crafted when craftingBonus stops to grow
	public static final float MAX_CRAFTING_DISCOUNT = .42f;// Max discount for crafting 
	
	public float getCraftingBonus(){
		return getCraftingBonus(unitsCrafted);
	}
	
	public static float getCraftingBonus(int unitsCrafted){
		return MathUtils.clamp(unitsCrafted / (float) MAX_UNITS_FOR_DISCOUNT, 0, MAX_CRAFTING_DISCOUNT);
	}
	
	public void orderCrafting(Craftable c, int amount, float[] t, ArrayList<SpecialTechnology> st){
		Resource[] ingridients = Heartstrings.get(c, Heartstrings.craftableProperties).ingridients;
		for (int i = 0; i < ingridients.length; ++i)
			if (!tryRemoveResource(ingridients[i], Heartstrings.getCraftingCost(c, ingridients[i], this, t, st, amount)))
				System.err.println("Bankrupt occured while withdrawing crafting order price");
		//manufactoryQueue.addLast(new CraftingOrder());
	}
	
	protected void onDestroy() {
		ownerFraction.unregisterStructure(this);
	}
	
	public float getRange(){
		return range;
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
	
	public Vector2 getPosition(){
		return position;
	}

	/*public void setPosition(Vector2 _n){
		position = _n;
	}*/
}
