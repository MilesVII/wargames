package com.milesseventh.wargames;

import java.util.ArrayList;

import com.milesseventh.wargames.Heartstrings.SpecialTechnology;
import com.milesseventh.wargames.Heartstrings.Technology;
import com.milesseventh.wargames.SpecialTechnologyProperties.TechnologyRequirement;
import com.milesseventh.wargames.Structure.Resource;

public class Heartstrings {
	public enum Technology{
		FIREPOWER, 
		ARMOR, 
		ACCURACY, 
		SPEED, 
		CARGO, 
		ENGINEERING
	}
	public enum Craftable{
		SCIENCE, TRANSPORTER, BUILDER, FIGHTER, AMMO, MISSILE
	}
	public enum SpecialTechnology{
		BASIC_WARFARE,            //Allows building of fighters, ammo crafting; Req: ENG(5%)
		SIEGE_I, SIEGE_II,        //Allows squads to siege structures and conquer them; Req: BASIC_WARFARE, ARMOR(10%), ARMOR(40%), ACC(10%)
		FORTIFICATION,            //Allows squads to fortify position and defend a spot, acting like a portable MB; Req: BASIC_WARFARE, FP(10%), ARMOR(30%)
		MOBILE_ATTACK,            //Allows squads to attack other squads; Req: BASIC_WARFARE, ACC(5%)
		//COLUMN_INTERCEPTION,      //Allows squads to intercept other squads; Req: BASIC_WARFARE, ACC(20%)
		ADVANCED_WARFARE,         //Allows building of MB; Req: BASIC_WARFARE, ENG(10%), FP(30%)
		RADIO,                    //Allows building of radars; Req: ADVANCED_WARFARE, ENG(15%)
		AMD_I, AMD_II,            //Allows building of AMD stations; Req_1: ADVANCED_WARFARE, ENG(25%), ACC(30%); Allows building of laser AMD stations; Req_2: AMD_I, ENG(45%), ACC(60%) 
		ESPIONAGE,                //Allows faction to steal foreign special tecnologies by capturing enemy's radars; Req: RADIO, SIEGE, ENG(30%)
		STRATEGIC_WARFARE,        //Allows building of missile silos and missile crafting; Req: ADVANCED_WARFARE, ENG(40%), ACC(25%), FP(50%), SPD(25%)
		WARHEAD_FRAGMENTATION_I,  //Allows missiles' payload to fragmentate, increasing effective area and reducing chance to be shotdown by AMD; Req: SW, ENG(60%), FP(60%)
		WARHEAD_FRAGMENTATION_II, //Req: WF_I, ACC(35%), SPD(40%)
		FLARES,                   //Allows missiles to use decoy flares; Req: SW, RADIO, ENG(50%)
	} 
	
	//title, shortTitle, maxMarkup
	public static TechnologyProperties[] tProperties = {
		new TechnologyProperties("Firepower",   "FPW", 70f),
		new TechnologyProperties("Armor",       "ARM", 80f),
		new TechnologyProperties("Accuracy",    "ACC", 50f),
		new TechnologyProperties("Speed",       "SPD", 30f),
		new TechnologyProperties("Cargo load",  "CRG", 40f),
		new TechnologyProperties("Engineering", "ENG", 120f)
	};
	
	//title, description, investigationWorkamount, workamountMarkup, investigationPrice, 
	//priceMarkup, TechnologyRequirement[], SpecialTechnology[]
	public static SpecialTechnologyProperties[] stProperties = {
		//BASIC_WARFARE
		new SpecialTechnologyProperties("Basic warfare", 
			"Allows building of fighters, ammo crafting;", 
			100f, 0f, 20f, 0f, 
			new TechnologyRequirement[]{
				new TechnologyRequirement(Technology.ENGINEERING, .05f)
			}, 
			new SpecialTechnology[]{}),

		//SIEGE_I
		new SpecialTechnologyProperties("Siege I", 
			"Allows squads to siege structures and capture them;", 
			120f, 20f, 120f, 20f,
			new TechnologyRequirement[]{
				new TechnologyRequirement(Technology.ARMOR, .1f),
				new TechnologyRequirement(Technology.ACCURACY, .1f)
			}, 
			new SpecialTechnology[]{
				SpecialTechnology.BASIC_WARFARE
			}),
		
		//SIEGE_II
		new SpecialTechnologyProperties("Siege II", 
			"Allows squads to siege structures and capture them;", 
			130f, 25f, 320f, 25f,
			new TechnologyRequirement[]{
				new TechnologyRequirement(Technology.ARMOR, .4f)
			}, 
			new SpecialTechnology[]{
				SpecialTechnology.SIEGE_I
		}),
		
		//FORTIFICATION
		new SpecialTechnologyProperties("Fortification", 
			"Allows squads to fortify position and defend a spot, "
			+ "acting like a portable military base;", 
			100f, 15f, 270f, 40f,
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
			110f, 15f, 175f, 15f,
			new TechnologyRequirement[]{
				new TechnologyRequirement(Technology.ACCURACY, .05f)
			}, 
			new SpecialTechnology[]{
				SpecialTechnology.BASIC_WARFARE
			}
		),
		
		/*/COLUMN_INTERCEPTION
		new SpecialTechnologyProperties("Column interception", 
			"Allows squads to intercept other squads;", 
			115f, 10f, 210f, 10f,
			new TechnologyRequirement[]{
				new TechnologyRequirement(Technology.ACCURACY, .2f)
			}, 
			new SpecialTechnology[]{
				SpecialTechnology.MOBILE_ATTACK
			}
		),*/
		
		//ADVANCED_WARFARE
		new SpecialTechnologyProperties("Advanced warfare", 
			"Allows building of military bases;", 
			320f, 0f, 760f, 0f,
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
			170f, 0f, 410f, 0f,
			new TechnologyRequirement[]{
				new TechnologyRequirement(Technology.ENGINEERING, .15f)
			}, 
			new SpecialTechnology[]{
				SpecialTechnology.ADVANCED_WARFARE
			}
		),
		
		//AMD I
		new SpecialTechnologyProperties("Anti-Missile Defence I", 
			"Allows building of anti-missile defence systems;",
			220f, 0f, 510f, 0f,
			new TechnologyRequirement[]{
				new TechnologyRequirement(Technology.ARMOR, .3f),
				new TechnologyRequirement(Technology.ENGINEERING, .25f)
			}, 
			new SpecialTechnology[]{
				SpecialTechnology.ADVANCED_WARFARE
			}
		),
		
		//AMD II
		new SpecialTechnologyProperties("Anti-Missile Defence II", 
			"Allows building of laser AMD systems;",
			270f, 0f, 790f, 0f,
			new TechnologyRequirement[]{
				new TechnologyRequirement(Technology.ACCURACY, .6f),
				new TechnologyRequirement(Technology.ENGINEERING, .45f)
			}, 
			new SpecialTechnology[]{
				SpecialTechnology.RADIO,
				SpecialTechnology.AMD_I
			}
		),
		
		//ESPIONAGE
		new SpecialTechnologyProperties("Industrial espionage", 
			"Allows faction to steal foreign special tecnologies "
			+ "by capturing enemy's radars",
			170f, 27f, 310f, 170f,
			new TechnologyRequirement[]{
				new TechnologyRequirement(Technology.ENGINEERING, .3f)
			}, 
			new SpecialTechnology[]{
				SpecialTechnology.RADIO,
				SpecialTechnology.SIEGE_II
			}
		),
		
		//STRATEGIC_WARFARE
		new SpecialTechnologyProperties("Strategic warfare", 
			"Allows building of missile silos and missile crafting;",
			700f, 0f, 1740f, 0f,
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
		
		//WARHEAD_FRAGMENTATION_I
		new SpecialTechnologyProperties("Warhead fragmentation I", 
			"Allows missiles' payload to fragmentate, increasing"
			+ "effective area and reducing chance to be shotdown by AMD;",
			400f, 300f, 810f, 120f,
			new TechnologyRequirement[]{
				new TechnologyRequirement(Technology.ENGINEERING, .6f),
				new TechnologyRequirement(Technology.FIREPOWER, .6f)
			}, 
			new SpecialTechnology[]{
				SpecialTechnology.STRATEGIC_WARFARE
			}
		),
		
		//WARHEAD_FRAGMENTATION_II
		new SpecialTechnologyProperties("Warhead fragmentation II", 
			"\"HIGH JACK THIS FAGS\";",
			420f, 400f, 750f, 200f,
			new TechnologyRequirement[]{
				new TechnologyRequirement(Technology.ACCURACY, .35f),
				new TechnologyRequirement(Technology.SPEED, .4f)
			}, 
			new SpecialTechnology[]{
				SpecialTechnology.WARHEAD_FRAGMENTATION_I
			}
		),
		
		//FLARES
		new SpecialTechnologyProperties("Decoy flares", 
			"Allows missiles to use decoy flares;",
			170f, 70f, 170f, 70f,
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
			                        new float[] {1.2f, .7f},
			                        new Technology[] {Technology.ENGINEERING},
			                        new SpecialTechnology[] {},
			                        20),
			new CraftableProperties("Transporter", 
			                        "Transports cargo between structures: ore, metal, oil, fuel, ammo, or missiles",
			                        new Resource[] {Resource.METAL},
			                        new float[] {32f},
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
			                        new float[] {420f},
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
			                        new float[] {70f},
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
			                        	SpecialTechnology.SIEGE_I,
			                        	SpecialTechnology.SIEGE_II,
			                        	SpecialTechnology.ESPIONAGE
			                        },
			                        70),
			new CraftableProperties("Ammo", 
			                        "Allows fighters to do their job, transported via Transporters",
			                        new Resource[] {Resource.METAL, Resource.FUEL},
			                        new float[] {.7f, .1f},
			                        new Technology[] {
			                        	Technology.FIREPOWER
			                        },
			                        new SpecialTechnology[] {},
			                        2),
			new CraftableProperties("Missile", 
			                        "Nuclear missile, being launched by silos",
			                        new Resource[] {Resource.METAL},
			                        new float[] {700f},
			                        new Technology[] {
			                        	Technology.ACCURACY,
			                        	Technology.SPEED,
			                        	Technology.FIREPOWER
			                        },
			                        new SpecialTechnology[] {//Missile
			                        	SpecialTechnology.WARHEAD_FRAGMENTATION_I,
			                        	SpecialTechnology.WARHEAD_FRAGMENTATION_II,
			                        	SpecialTechnology.FLARES
			                        },
			                        1070)
	};
	
	//share of initial craftable's price with a regard of it's techs
	public static float getTechMarkup(float[] t, ArrayList<SpecialTechnology> st){
		float cost = 0;
		for (int i = 0; i < Technology.values().length; i++)
			cost += t[i] * tProperties[i].maxMarkup;
		for (int i = 0; i < stProperties.length; i++)
			if (st.contains(SpecialTechnology.values()[i]))
				cost += stProperties[i].priceMarkupInMetal;
		return cost;
	}
	public static float getCraftingCost(CraftingDialog cd, Resource r, int count){
		return getCraftingCost(cd.selected, r, cd.selectedT, cd.selectedST, count);
	}
	public static float getCraftingCost(Craftable unit, Resource res, float[] t, ArrayList<SpecialTechnology> st, int count){
		switch(unit){
		case AMMO:
		case SCIENCE:
			return ((get(unit, craftableProperties).getSingleCraftingCost(res) + getTechMarkup(t, st)) * count);
		case BUILDER:
		case FIGHTER:
		case MISSILE:
		case TRANSPORTER:
			float cost = 0, craftingCost = get(unit, craftableProperties).getSingleCraftingCost(res) + getTechMarkup(t, st);
			for (int i = 0; i < count; i++)
				cost += craftingCost;
			return cost;
		default:
			return -1;
		}
	}
	
	public static float getWorkamount(Unit u, float[] t, ArrayList<SpecialTechnology> st){
		return getWorkamount(fromUnitType(u.type), t, st);
	}
	public static float getWorkamount(Craftable c, float[] t, ArrayList<SpecialTechnology> st){
		float wa = get(c, craftableProperties).workamount;
		for (int i = 0; i < Technology.values().length; ++i)
			wa += tProperties[i].maxMarkup * t[i];
		for (SpecialTechnology i : st)
			wa += get(i, stProperties).workamountMarkup;
		
		return wa;
	}
	
	public static int getMaxCraftingOrder(Craftable unit, Structure manufacturer, float[] t, ArrayList<SpecialTechnology> st){
		int order, minOrder = Integer.MAX_VALUE;//INF
		for (Resource r: get(unit, craftableProperties).ingridients){
			order = (int) Math.floor(manufacturer.getResource(r) / (get(unit, craftableProperties).getSingleCraftingCost(r) + getTechMarkup(t, st)));
			while(getCraftingCost(unit, r, t ,st, order) <= manufacturer.getResource(r))
				order ++;
			minOrder = Math.min(minOrder, order - 1);
		}
		return minOrder;
	}

	public static float getRepairCostInMetal(Unit u){
		if (!u.isDamaged())
			return 0;
		
		return ((u.getMaxCondition() - u.condition) / u.getMaxCondition()) *      //Damage fraction
		        getCraftingCost(fromUnitType(u.type), 
		        	Structure.Resource.METAL,
		        	u.techLevel, u.st, 1);                                        //Cost of bulding of a new analog unit
	}

	private static final ArrayList<SpecialTechnology> imperialistscums = new ArrayList<SpecialTechnology>();
	public static float getUpgradeCostInMetal(Unit u, float[] nt, ArrayList<SpecialTechnology> stToAdd){
		imperialistscums.clear();
		imperialistscums.addAll(u.st);
		imperialistscums.addAll(stToAdd);
		
		return 
		getCraftingCost(fromUnitType(u.type), Structure.Resource.METAL, nt, imperialistscums, 1) -
		getCraftingCost(fromUnitType(u.type), Structure.Resource.METAL, u.techLevel, u.st, 1);
	}
	
	public static float getUpgradeWorkamount(Unit u, float[] nt, ArrayList<SpecialTechnology> stToAdd){
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
