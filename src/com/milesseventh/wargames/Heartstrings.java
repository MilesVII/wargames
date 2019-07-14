package com.milesseventh.wargames;

import java.util.ArrayList;

import com.milesseventh.wargames.dialogs.CraftingDialog;
import com.milesseventh.wargames.properties.CraftableProperties;
import com.milesseventh.wargames.properties.ResourceProperties;
import com.milesseventh.wargames.properties.SpecialTechnologyProperties;
import com.milesseventh.wargames.properties.SpecialTechnologyProperties.TechnologyRequirement;
import com.milesseventh.wargames.properties.StructureProperties;
import com.milesseventh.wargames.properties.TechnologyProperties;
import com.milesseventh.wargames.properties.UnitProperties;

public class Heartstrings {
	public enum Technology{
		FIREPOWER, ARMOR, ACCURACY, SPEED, CARGO, ENGINEERING
	}
	public enum Craftable{
		SCIENCE, TRANSPORTER, BUILDER, FIGHTER, AMMO, MISSILE
	}
	public enum SpecialTechnology{
		BASIC_WARFARE,            //Allows building of fighters, ammo crafting; Req: ENG(5%)
		SIEGE,                    //Allows squads to siege structures and conquer them; Req: BASIC_WARFARE, ARMOR(10%), ARMOR(40%), ACC(10%)
		FORTIFICATION,            //Allows squads to fortify position and reduce amount of damage taken; Req: BASIC_WARFARE, FP(10%), ARMOR(30%)
		MOBILE_ATTACK,            //Allows squads to attack on the go; Req: BASIC_WARFARE, ACC(5%)
		ADVANCED_WARFARE,         //Allows building of MB; Req: BASIC_WARFARE, ENG(10%), FP(30%)
		RADIO,                    //Allows building of radars; Req: ADVANCED_WARFARE, ENG(15%)
		AMD,                      //Allows building of AMD stations; ADVANCED_WARFARE, ENG(25%), ACC(30%)
		ESPIONAGE,                //Allows faction to steal foreign special tecnologies by capturing enemy's radars; Req: RADIO, SIEGE, ENG(30%)
		STRATEGIC_WARFARE,        //Allows building of missile silos and missile crafting; Req: ADVANCED_WARFARE, ENG(40%), ACC(25%), FP(50%), SPD(25%)
		WARHEAD_FRAGMENTATION,    //Allows missiles' payload to fragmentate, increasing effective area and reducing chance to be shotdown by AMD; Req: SW, ENG(60%), FP(60%)
		FLARES,                   //Allows missiles to use decoy flares; Req: SW, RADIO, ENG(50%)
	}

	public static final float INTERACTION_DISTANCE2 = 120; //10
	public static final float STRUCTURE_BUILDING_MIN_DISTANCE2 = INTERACTION_DISTANCE2 * 2;
	public static final float STRUCTURE_INTERACTION_COLLISION_DISTANCE2 = INTERACTION_DISTANCE2 * .12f;
	
	public static final int MISSILE_ACTIVE_STORAGE_CAPACITY = 3;
	public static final float MISSILE_MOUNTING_SPEED_MIN = .02f;
	public static final float MISSILE_MOUNTING_SPEED_MAX = .1f;
	public static final float MISSILE_FUEL_CONSUMPTION_RELATIVE = 7000f;
	public static final float MISSILE_FUEL_CONSUMPTION_DISCOUNT_MAX = .25f;
	public static final float MISSILE_SPEED_MIN = 8f;
	public static final float MISSILE_SPEED_MAX = 24f;
	public static final float MISSILE_BLAST_RADIUS_MIN = 32f;
	public static final float MISSILE_BLAST_RADIUS_MAX = 128f;
	public static final float MISSILE_BLAST_CORE_FRACTION = .32f;
	public static final float MISSILE_EXPLOSION_DAMAGE_MIN = 200f;
	public static final float MISSILE_EXPLOSION_DAMAGE_MAX = 1500f;
	public static final float MISSILE_SHAKE_MAX = 32f;

	public static final float DEBUG_STRUCTURE_DAMAGE = 32f;
	public static final float SQUAD_ATTACK_RANGE_MIN = 15f;
	public static final float SQUAD_ATTACK_RANGE_MAX = 25f;
	public static final float SQUAD_FORTIFICATION_PER_SECOND = .1f;
	public static final float UNIT_FIREPOWER_MIN = 7f;
	public static final float UNIT_FIREPOWER_MAX = 14f;
	public static final float UNIT_MAX_ANNEXATION_CHANCE_PER_SECOND = .5f;
	public static final float STRUCTURE_CONDITION_ANNEXATION_THRESHOLD_RELATIVE = .42f;
	
	//name, sign, description
	public static ResourceProperties[] rProperties = {
		new ResourceProperties("Metal Ingot", "M",   "Metal slug. Main building material and currency"),
		new ResourceProperties("Ammo Boxes",  "pcs", "Ammunition. Used by squads and structures to defend themselves"),
		new ResourceProperties("Fuel",        "bbl", "Fuel. used by squads when moving")
	};
	//title, shortTitle, maxMarkup
	public static TechnologyProperties[] tProperties = {
		new TechnologyProperties("Firepower",   "FPW", 70000), //Improves potential damage from squad's fire
		new TechnologyProperties("Armor",       "ARM", 80000), //Improves vitality of units
		new TechnologyProperties("Accuracy",    "ACC", 50000), //Improves fire efficiency and fighting radius
		new TechnologyProperties("Speed",       "SPD", 30000), //Improves speed and reduces fuel consumption
		new TechnologyProperties("Cargo load",  "CRG", 40000), //Improves transporters' capacity
		new TechnologyProperties("Engineering", "ENG", 120000) //Improves effectiveness of cities and reduces building costs
	};
	//title, fightingRange, maxCondition, craftSpeed, buildingPrice (In Metal)
	public static StructureProperties[] structureProperties = {
		new StructureProperties("City",             22f, 1260f, .8f,  700000, "Universal sructure. Medium defence"),
		new StructureProperties("Mining Outpost",     0,  210f,   0,  120000, "Ore miner. No defence"),
		new StructureProperties("Military Outpost", 32f,  600f, .2f,  420000, "Military forification. Can build own units, has strong defence"),
		new StructureProperties("Missile Silo",     12f,  128f,   0, 1700000, "Missile silo. Weak defence"),
		new StructureProperties("Radar",            12f,  210f,   0,  300000, "Radar. Controls missile's flight, has weak defence"),
		new StructureProperties("AMD",              17f,  360f,   0,  750000, "Anti-Missile Defence. Can shoot down incoming missiles. Has weak defence against troops")
	};
	//name, fuelConsumption, minMaxCondition, maxMaxCondition, minSpeed, maxSpeed, receiveFireChance
	public static UnitProperties[] uProperties = {
		new UnitProperties("Fighter",      300f, 350, 750,  3,  7, 70),
		new UnitProperties("Transporter",  120f,  60, 375,  5, 10, 25),
		new UnitProperties("MCV",         2000f, 160, 260,  2,  4,  5)
	};
	//title, description, investigationWorkamount, workamountMarkup, investigationPrice, 
	//priceMarkup, TechnologyRequirement[], SpecialTechnology[]
	public static SpecialTechnologyProperties[] stProperties = {
		//BASIC_WARFARE
		new SpecialTechnologyProperties("Basic warfare", 
			"Allows building of fighters, ammo crafting;", 
			100000, 0, 20000, 0, 
			new TechnologyRequirement[]{
				new TechnologyRequirement(Technology.ENGINEERING, .05f)
			}, 
			new SpecialTechnology[]{}),

		//SIEGE
		new SpecialTechnologyProperties("Siege", 
			"Allows squads to siege structures and capture them;", 
			120000, 20000, 120000, 20000,
			new TechnologyRequirement[]{
				new TechnologyRequirement(Technology.ARMOR, .1f),
				new TechnologyRequirement(Technology.ACCURACY, .1f)
			}, 
			new SpecialTechnology[]{
				SpecialTechnology.BASIC_WARFARE
			}),
		
		//FORTIFICATION
		new SpecialTechnologyProperties("Fortification", 
			"Allows squads to fortify position and reduce amount of damage taken", 
			64000, 15000, 270000, 40000,
			new TechnologyRequirement[]{
				new TechnologyRequirement(Technology.FIREPOWER, .1f),
				new TechnologyRequirement(Technology.ARMOR, .3f)
			}, 
			new SpecialTechnology[]{
				SpecialTechnology.BASIC_WARFARE
		}),
		
		//MOBILE_ATTACK
		new SpecialTechnologyProperties("Column attack", 
			"Allows squads to attack other squads;", 
			110000, 15000, 175000, 15000,
			new TechnologyRequirement[]{
				new TechnologyRequirement(Technology.ACCURACY, .05f)
			}, 
			new SpecialTechnology[]{
				SpecialTechnology.BASIC_WARFARE
			}
		),
		
		//ADVANCED_WARFARE
		new SpecialTechnologyProperties("Advanced warfare", 
			"Allows building of military bases;", 
			320000, 0, 760000, 0,
			new TechnologyRequirement[]{
				new TechnologyRequirement(Technology.ENGINEERING, .1f),
				new TechnologyRequirement(Technology.FIREPOWER, .3f)
			}, 
			new SpecialTechnology[]{
				SpecialTechnology.BASIC_WARFARE
			}
		),
		
		//RADIO
		new SpecialTechnologyProperties("Electronic warfare", 
			"Allows building of radars", 
			170000, 0, 410000, 0,
			new TechnologyRequirement[]{
				new TechnologyRequirement(Technology.ENGINEERING, .15f)
			}, 
			new SpecialTechnology[]{
				SpecialTechnology.ADVANCED_WARFARE
			}
		),
		
		//AMD
		new SpecialTechnologyProperties("Anti-Missile Defence", 
			"Allows building of anti-missile defence systems;",
			220000, 0, 510000, 0,
			new TechnologyRequirement[]{
				new TechnologyRequirement(Technology.ARMOR, .3f),
				new TechnologyRequirement(Technology.ENGINEERING, .25f)
			}, 
			new SpecialTechnology[]{
				SpecialTechnology.STRATEGIC_WARFARE
			}
		),
		
		//ESPIONAGE
		new SpecialTechnologyProperties("Industrial espionage", 
			"Allows faction to steal foreign special tecnologies "
			+ "by capturing enemy's radars",
			170000, 27000, 310000, 170000,
			new TechnologyRequirement[]{
				new TechnologyRequirement(Technology.ENGINEERING, .3f)
			}, 
			new SpecialTechnology[]{
				SpecialTechnology.RADIO,
				SpecialTechnology.SIEGE//II
			}
		),
		
		//STRATEGIC_WARFARE
		new SpecialTechnologyProperties("Strategic warfare", 
			"Allows building of missile silos and missile crafting;",
			700000, 0, 1740000, 0,
			new TechnologyRequirement[]{
				new TechnologyRequirement(Technology.ENGINEERING, .4f),
				new TechnologyRequirement(Technology.ACCURACY, .25f),
				new TechnologyRequirement(Technology.FIREPOWER, .5f),
				new TechnologyRequirement(Technology.SPEED, .25f)
			}, 
			new SpecialTechnology[]{
				SpecialTechnology.ADVANCED_WARFARE
			}
		),
		
		//WARHEAD_FRAGMENTATION
		new SpecialTechnologyProperties("Warhead fragmentation", 
			"Allows missiles' payload to fragmentate, increasing"
			+ "effective area and reducing chance to be shotdown by AMD;",
			400000, 300000, 810000, 120000,
			new TechnologyRequirement[]{
				new TechnologyRequirement(Technology.ENGINEERING, .6f),
				new TechnologyRequirement(Technology.FIREPOWER, .6f)
			}, 
			new SpecialTechnology[]{
				SpecialTechnology.STRATEGIC_WARFARE
			}
		),
		
		//FLARES
		new SpecialTechnologyProperties("Decoy flares", 
			"Allows missiles to use decoy flares;",
			170000, 70000, 170000, 70000,
			new TechnologyRequirement[]{
				new TechnologyRequirement(Technology.FIREPOWER, .5f),
			}, 
			new SpecialTechnology[]{
				SpecialTechnology.RADIO,	
				SpecialTechnology.STRATEGIC_WARFARE
			}
		)
	};
	
	public static CraftableProperties[] craftableProperties = {
			new CraftableProperties("Science data", 
			                        "Allows doing investigations",
			                        new Resource[] {Resource.METAL, Resource.FUEL},
			                        new int[] {1000, 700},
			                        new Technology[] {Technology.ENGINEERING},
			                        new SpecialTechnology[] {},
			                        20),
			new CraftableProperties("Transporter", 
			                        "Transports cargo between structures: ore, metal, oil, fuel, ammo, or missiles",
			                        new Resource[] {Resource.METAL},
			                        new int[] {32000},
			                        new Technology[] {
			                        	Technology.ARMOR,
			                        	Technology.SPEED,
			                        	Technology.CARGO,
			                        },
			                        new SpecialTechnology[] {},
			                        30),
			new CraftableProperties("Builder", 
			                        "Special vehicle that is able to create new structures \nusing resources that are being transported using transporters",
			                        new Resource[] {Resource.METAL},
			                        new int[] {420000},
			                        new Technology[] {
			                        	Technology.ARMOR,
			                        	Technology.SPEED,
			                        	Technology.ENGINEERING,
			                        },
			                        new SpecialTechnology[] {},
			                        100),
			new CraftableProperties("Fighter", 
			                        "Protects column and is able to attack structures using ammo",
			                        new Resource[] {Resource.METAL},
			                        new int[] {70000},
			                        new Technology[] {
			                        	Technology.ACCURACY,
			                        	Technology.ARMOR,
			                        	Technology.FIREPOWER,
			                        	Technology.SPEED,
			                        },
			                        new SpecialTechnology[] {//Fighter
			                        	//SpecialTechnology.COLUMN_INTERCEPTION,
			                        	SpecialTechnology.FORTIFICATION,
			                        	SpecialTechnology.MOBILE_ATTACK,
			                        	SpecialTechnology.SIEGE,
			                        	SpecialTechnology.ESPIONAGE
			                        },
			                        70),
			new CraftableProperties("Ammo", 
			                        "Allows fighters to do their job, transported via Transporters",
			                        new Resource[] {Resource.METAL, Resource.FUEL},
			                        new int[] {700, 100},
			                        new Technology[] {
			                        	//Technology.FIREPOWER
			                        },
			                        new SpecialTechnology[] {},
			                        2),
			new CraftableProperties("Missile", 
			                        "Ballistic Missile. Can be launched from Missile Silo. High jack this buddy!",
			                        new Resource[] {Resource.METAL},
			                        new int[] {700000},
			                        new Technology[] {
			                        	Technology.ACCURACY,
			                        	Technology.SPEED,
			                        	Technology.FIREPOWER
			                        },
			                        new SpecialTechnology[] {//Missile
			                        	SpecialTechnology.WARHEAD_FRAGMENTATION,//_I,
			                        	//SpecialTechnology.WARHEAD_FRAGMENTATION_II,
			                        	SpecialTechnology.FLARES
			                        },
			                        770)
	};
	
	//share of initial craftable's price with a regard of it's techs
	public static int getTechMarkup(float[] t, ArrayList<SpecialTechnology> st){
		int cost = 0;
		for (int i = 0; i < Technology.values().length; i++)
			cost += t[i] * tProperties[i].maxMarkup;
		for (int i = 0; i < stProperties.length; i++)
			if (st.contains(SpecialTechnology.values()[i]))
				cost += stProperties[i].priceMarkupInMetal;
		return cost;
	}
	public static int getCraftingCost(CraftingDialog cd, Resource r, int count){
		return getCraftingCost(cd.selected, r, cd.selectedT, cd.selectedST, count);
	}
	public static int getCraftingCost(Craftable unit, Resource res, float[] t, ArrayList<SpecialTechnology> st, int count){
		//Count only those technologies that used by unit
		for (int i = 0; i < t.length; ++i){
			boolean keepTech = false;
			Technology[] at = get(unit, craftableProperties).availableTechs;
			for (int j = 0; j < at.length; ++j)
				if (at[j] == Technology.values()[i])
					keepTech = true;
			if (!keepTech)
				t[i] = 0;
		}
		
		return (get(unit, craftableProperties).getSingleCraftingCost(res) + getTechMarkup(t, st)) * count;
	}
	
	public static int getWorkamount(Unit u, float[] t, ArrayList<SpecialTechnology> st){
		return getWorkamount(fromUnitType(u.type), t, st);
	}
	public static int getWorkamount(Craftable c, float[] t, ArrayList<SpecialTechnology> st){
		int wa = get(c, craftableProperties).workamount;
		for (int i = 0; i < Technology.values().length; ++i)
			wa += tProperties[i].maxMarkup * t[i];
		for (SpecialTechnology i : st)
			wa += get(i, stProperties).workamountMarkup;
		
		return wa;
	}
	
	public static int getMaxCraftingOrder(Craftable unit, Structure manufacturer, float[] t, ArrayList<SpecialTechnology> st){
		int order, minOrder = Integer.MAX_VALUE;//INF
		for (Resource r: get(unit, craftableProperties).ingridients){
			order = (int) Math.floor(manufacturer.resources.get(r) / (get(unit, craftableProperties).getSingleCraftingCost(r) + getTechMarkup(t, st)));
			while(getCraftingCost(unit, r, t ,st, order) <= manufacturer.resources.get(r))
				order ++;
			minOrder = Math.min(minOrder, order - 1);
		}
		return minOrder;
	}

	public static int getRepairCostInMetal(Unit u){
		if (!u.isDamaged())
			return 0;
		
		return Math.round((u.getMaxCondition() - u.condition) / u.getMaxCondition()) *      //Damage fraction
		       getCraftingCost(fromUnitType(u.type), Resource.METAL, u.techLevel, u.st, 1); //Cost of bulding of a new analog unit
	}

	private static final ArrayList<SpecialTechnology> imperialistscums = new ArrayList<SpecialTechnology>();
	public static int getUpgradeCostInMetal(Unit u, float[] nt, ArrayList<SpecialTechnology> stToAdd){
		imperialistscums.clear();
		imperialistscums.addAll(u.st);
		imperialistscums.addAll(stToAdd);
		
		return 
		getCraftingCost(fromUnitType(u.type), Resource.METAL, nt, imperialistscums, 1) -
		getCraftingCost(fromUnitType(u.type), Resource.METAL, u.techLevel, u.st, 1);
	}
	
	public static int getUpgradeWorkamount(Unit u, float[] nt, ArrayList<SpecialTechnology> stToAdd){
		imperialistscums.clear();
		imperialistscums.addAll(u.st);
		imperialistscums.addAll(stToAdd);

		return getWorkamount(u, nt, imperialistscums) - getWorkamount(u, u.techLevel, u.st);
	}
	
	public static Craftable fromUnitType(Unit.Type type){
		switch (type){
		case BUILDER:
			return Craftable.BUILDER;
		case FIGHTER:
			return Craftable.FIGHTER;
		case TRANSPORTER:
			return Craftable.TRANSPORTER;
		}
		return Craftable.FIGHTER;
	}
	
	public static <E extends Enum<E>, S> S get(E e, S[] s){
		try{
			return s[e.ordinal()];
		} catch (ArrayIndexOutOfBoundsException ex) {
			ex.printStackTrace();
		}
		return null;
	}
	public static <E extends Enum<E>> float get(E e, float[] s){
		try{
			return s[e.ordinal()];
		} catch (ArrayIndexOutOfBoundsException ex) {
			ex.printStackTrace();
		}
		return -1;
	}
}
