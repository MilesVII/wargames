package com.milesseventh.wargames;

import com.badlogic.gdx.math.Vector2;

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
	
	private float[] resources = {0, 0, 0, 0, 0, 0};
	public enum Resource {
		ORE, METAL, AMMO, MISSILE, OIL, FUEL
	}
	
	public enum StructureType{
		CITY, MINER, ML, RADAR, AMD, MB 
	}
	//                                             C   M   ML   R  AMD   MB
	public static final float[] DEFAULT_RANGES = { 22,  0, 17, 12,  27,  42};
	public static final float[] DEFAULT_MAXCDS = {420, 70, 42, 70, 120, 200};
	public static final int[]   PIEMENU_ACTCNT = {  5,  0,  0,  0,   0,   0};
	
	private float range;//Radius of circle that will be added to fraction's territory
	public Fraction ownerFraction;//ID of fraction that owns this unit
	private float condition, maxCondition;
	private Vector2 position;
	public StructureType type;
	
	public static final Croupfuck PIEMENU_ACTIONS_CITY = new Croupfuck(){
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
		condition = maxCondition = DEFAULT_MAXCDS[type.ordinal()];
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
	
	protected void onDestroy() {
		ownerFraction.unregisterStructure(this);
	}
	
	public float getRange(){
		return range;
	}
	
	public void hit(float _damage){
		condition -= _damage;
		if (condition <= 0){
			onDestroy();
		}
	}
	
	public void repair(float _repair){
		condition += _repair;
		if (condition > maxCondition){
			condition = maxCondition;
		}
	}
	
	public Vector2 getPosition(){
		return position;
	}

	/*public void setPosition(Vector2 _n){
		position = _n;
	}*/
}
