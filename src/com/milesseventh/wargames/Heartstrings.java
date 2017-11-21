package com.milesseventh.wargames;

import java.util.ArrayList;

import com.milesseventh.wargames.Structure.Resource;

public class Heartstrings {
	public enum Technology{
		FIREPOWER, ARMOR, ACCURACY, SPEED, CARGO, ENGINEERING
	}
	public enum Craftable{
		SCIENCE, TRANSPORTER, BUILDER, FIGHTER, AMMO, MISSILE
	}
	public enum SpecialTechnology{
		BASIC_WARFARE,          //Allows building of fighters, ammo crafting; Req: ENG(5%)
		SIEGE_I, SIEGE_II,      //Allows squads to siege structures and conquer them; Req: BASIC_WARFARE, ARMOR(10%), ARMOR(40%), ACC(10%)
		FORTIFICATION,          //Allows squads to fortify position and defend a spot, acting like a portable MB; Req: BASIC_WARFARE, FP(10%), ARMOR(30%)
		MOBILE_ATTACK,          //Allows squads to attack other squads; Req: BASIC_WARFARE, ACC(5%)
		COLUMN_INTERCEPTION,    //Allows squads to intercept other squads; Req: BASIC_WARFARE, ACC(20%)
		ADVANCED_WARFARE,       //Allows building of MB; Req: BASIC_WARFARE, ENG(10%), FP(30%)
		RADIO,                  //Allows building of radars; Req: ADVANCED_WARFARE, ENG(15%)
		AMD_I, AMD_II,          //Allows building of AMD stations; Req_1: ADVANCED_WARFARE, ENG(25%), ACC(30%); Allows building of laser AMD stations; Req_2: AMD_I, ENG(45%), ACC(60%) 
		ESPIONAGE,              //Allows fraction to steal foreign special tecnologies by capturing enemy's radars; Req: RADIO, SIEGE, ENG(30%)
		STRATEGIC_WARFARE,      //Allows building of missile silos and missile crafting; Req: ADVANCED_WARFARE, ENG(40%), ACC(25%), FP(50%), SPD(25%)
		WARHEAD_FRAGMENTATION_I,//Allows missiles' payload to fragmentate, increasing effective area and reducing chance to be shotdown by AMD; Req: SW, ENG(60%), FP(60%)
		WARHEAD_FRAGMENTATION_II,//Req: WF_I, ACC(35%), SPD(40%)
		FLARES,                 //Allows missiles to use decoy flares; Req: SW, RADIO, ENG(50%)
	}
	
	//In metal
	public static float[] specialTechnologyCost = {
		0f,
		20f,//Siege
		25f,
		40f,//Fortification
		15f,//Mobile Attack
		10f,//Column Interception
		0f,
		0f,
		0f,
		0f,
		170f,//Espionage
		0f,
		120f,//Fragmentation
		200f,
		70f//Flares
	}, technologyMaxCost = {
		70f,//FPW
		40f,//ARM
		50f,//ACC
		30f,//SPD
		40f,//CRG
		120f//ENG
	};
	
	
	
	public static String[] technologyTitles = {
		"Firepower", "Armor", "Accuracy", "Speed", "Cargo load", "Engineering"
	};
	
	public static String[] craftableTitles = {
			"Science data",
			"Transporter",
			"Builder",
			"Fighter",
			"Ammo",
			"Missile"
	};
	public static String[] craftablePrompts = {
			"Allows doing investigations",//Science data
			"Transports cargo between structures, such as ore, metal, oil, fuel, ammo, or missiles",//Transporter
			"Special vehicle that is able to create new structures \nusing resources that are being transported using transporters",//Builder
			"Protects column and is able to attack structures using ammo",//Fighter
			"Allows fighters to do their job, transported via Transporters",//Ammo
			"Nuclear missile, being launched by silos"//Missile
	};
	public static Technology[][] availableCraftableTechs = {
			{//Science
				Technology.ENGINEERING
			},
			{//Transporter
				Technology.ARMOR,
				Technology.SPEED,
				Technology.CARGO,
			},
			{//Builder
				Technology.ARMOR,
				Technology.SPEED,
				Technology.ENGINEERING,
			},
			{//Fighter
				Technology.ACCURACY,
				Technology.ARMOR,
				Technology.FIREPOWER,
				Technology.SPEED,
			},
			{//Ammmo
				Technology.FIREPOWER
			},
			{//Missile
				Technology.ACCURACY,
				Technology.SPEED,
				Technology.FIREPOWER
			}
	};
	public static Resource[][] craftableIngridients = {
			{//Science
				Resource.METAL,
				Resource.FUEL
			},
			{//Transporter
				Resource.METAL
			},
			{//Builder
				Resource.METAL
			},
			{//Fighter
				Resource.METAL
			},
			{//Ammmo
				Resource.METAL,
				Resource.FUEL
			},
			{//Missile
				Resource.METAL
			}
	};
	public static float[][] craftableRelativeCosts = {
			{//Science
				1.2f,//M
				0.7f//F
			},
			{//Transporter
				32f//M
			},
			{//Builder
				420f//M
			},
			{//Fighter
				70f//M
			},
			{//Ammmo
				.7f,//M
				.1f//F
			},
			{//Missile
				700f//M
			}
	};
	
	public static float getSingleCraftingCost(Craftable unit, Resource res){
		Resource[] ings = get(unit, craftableIngridients);
		for (int i = 0; i < ings.length; i++){
			if (ings[i] == res)
				return get(unit, craftableRelativeCosts)[i];
		}
		return -1;
	}
	
	public static float getTechCost(float[] t, ArrayList<SpecialTechnology> st){
		float cost = 0;
		/*for (Technology horsey: Technology.values()){       =C
			cost += get(horsey, t) * get(horsey, technologyMaxCost);
		}*/
		for (int i = 0; i < Technology.values().length; i++)
			cost += t[i] * technologyMaxCost[i];
		for (int i = 0; i< specialTechnologyCost.length; i++)
			if (st.contains(SpecialTechnology.values()[i]))
				cost += specialTechnologyCost[i];
		return cost;
	}
	
	public static float getCraftingCost(Craftable unit, Resource res, Structure manufacturer, float[] t, ArrayList<SpecialTechnology> st, int count){
		switch(unit){
		case AMMO:
		case SCIENCE:
			return ((getSingleCraftingCost(unit, res) + getTechCost(t, st)) * (1 - manufacturer.getCraftingBonus()) * count);
		case BUILDER:
		case FIGHTER:
		case MISSILE:
		case TRANSPORTER:
			float cost = 0, craftingCost = getSingleCraftingCost(unit, res) + getTechCost(t, st);
			for (int i = 0; i < count; i++)
				cost += craftingCost * (1 - Structure.getCraftingBonus(manufacturer.unitsCrafted + i));
			return cost;
		default:
			return -1;
		}
	}
	
	public static int getMaxCraftingOrder(Craftable unit, Structure manufacturer, float[] t, ArrayList<SpecialTechnology> st){
		int order, minOrder = Integer.MAX_VALUE;//INF
		for (Resource r: get(unit, craftableIngridients)){
			order = (int) Math.floor(manufacturer.getResource(r) / (getSingleCraftingCost(unit, r) + getTechCost(t, st)));
			while(getCraftingCost(unit, r, manufacturer, t ,st, order) <= manufacturer.getResource(r))
				order ++;
			minOrder = Math.min(minOrder, order - 1);
		}
		return minOrder;
	}
	
	public static SpecialTechnology[][] availableCraftablesST = { 
			{},//Science
			{},//Transporter
			{},//Builder
			{//Fighter
				SpecialTechnology.COLUMN_INTERCEPTION,
				SpecialTechnology.ESPIONAGE,
				SpecialTechnology.FORTIFICATION,
				SpecialTechnology.MOBILE_ATTACK,
				SpecialTechnology.SIEGE_I,
				SpecialTechnology.SIEGE_II
			},
			{},//Ammmo
			{//Missile
				SpecialTechnology.WARHEAD_FRAGMENTATION_I,
				SpecialTechnology.WARHEAD_FRAGMENTATION_II,
				SpecialTechnology.FLARES
			}
	};
	public static String[] specialTechnologyTitles = {
			"Basic warfare", "Siege I", "Siege II", "Fortification", "Column attack", "Column interception", 
			"Advanced warfare", "Electronic warfare", "Anti-Missile Defence I", "Anti-Missile Defence II", 
			"Industrial espionage", "Strategic warfare", "Warhead fragmentation I", "Warhead fragmentation II", "Decoy flares"
		};
	public static String[] specialTechnologyPrompts = {
			"Reqs:\n"//Basic warfare
			+ "> ENG: 5%\n"
			+ "Allows building of fighters, ammo crafting;", 
			
			"Reqs:\n> "//Siege I
			+ specialTechnologyTitles[SpecialTechnology.BASIC_WARFARE.ordinal()] + "\n"
			+ "> ARM: 10%\n"
			+ "> ACC: 10%\n"
			+ "Allows squads to siege structures and capture them;", 
			
			"Reqs:\n> "//Siege II
			+ specialTechnologyTitles[SpecialTechnology.SIEGE_I.ordinal()] + "\n"
			+ "> ARM: 40%\n"
			+ "Allows squads to siege structures and capture them;", 
			
			"Reqs:\n> "//Fortification
			+ specialTechnologyTitles[SpecialTechnology.BASIC_WARFARE.ordinal()] + "\n"
			+ "> FPW: 10%\n"
			+ "> ARM: 30%\n"
			+ "Allows squads to fortify position and defend a spot, "
			+ "acting like a portable military base;", 
			
			"Reqs:\n> "//Column attack
			+ specialTechnologyTitles[SpecialTechnology.BASIC_WARFARE.ordinal()] + "\n"
			+ "> ACC: 10%\n"
			+ "Allows squads to attack other squads;", 
			
			"Reqs:\n> "//Column interception
			+ specialTechnologyTitles[SpecialTechnology.MOBILE_ATTACK.ordinal()] + "\n"
			+ "> ACC: 20%\n"
			+ "Allows squads to intercept other squads;", 
			
			"Reqs:\n> "//Advanced warfare
			+ specialTechnologyTitles[SpecialTechnology.BASIC_WARFARE.ordinal()] + "\n"
			+ "> ENG: 10%\n"
			+ "> FPW: 30%\n"
			+ "Allows bulding of military bases;", 
			
			"Reqs:\n> "//Electronic warfare
			+ specialTechnologyTitles[SpecialTechnology.ADVANCED_WARFARE.ordinal()] + "\n"
			+ "> ENG: 15%\n"
			+ "Allows building of radars", 
			
			"Reqs:\n> "//AMD I
			+ specialTechnologyTitles[SpecialTechnology.ADVANCED_WARFARE.ordinal()] + "\n"
			+ "> ENG: 25%\n"
			+ "> ACC: 30%\n"
			+ "Allows building of anti-missile defence systems;",
			
			"Reqs:\n> "//AMD II
			+ specialTechnologyTitles[SpecialTechnology.RADIO.ordinal()] + "\n"
			+ "> ENG: 45%\n"
			+ "> ACC: 60%\n"
			+ "Allows building of laser AMD systems;",
			
			"Reqs:\n> "//Espionage
			+ specialTechnologyTitles[SpecialTechnology.RADIO.ordinal()] + "\n> "
			+ specialTechnologyTitles[SpecialTechnology.SIEGE_I.ordinal()] + "\n"
			+ "> ENG: 30%\n"
			+ "Allows fraction to steal foreign special tecnologies "
			+ "by capturing enemy's radars",
			
			"Reqs:\n> "//Strategic warfare
			+ specialTechnologyTitles[SpecialTechnology.ADVANCED_WARFARE.ordinal()] + "\n"
			+ "> ENG: 40%\n"
			+ "> ACC: 25%\n"
			+ "> FPW: 50%\n"
			+ "> SPD: 25%\n"
			+ "Allows building of missile silos and missile crafting;",
			
			"Reqs:\n> "//Warhead fragmentation I
			+ specialTechnologyTitles[SpecialTechnology.STRATEGIC_WARFARE.ordinal()] + "\n"
			+ "> ENG: 60%\n"
			+ "> FPW: 60%\n"
			+ "Allows missiles' payload to fragmentate, increasing\n"
			+ "effective area and reducing chance to be shotdown by AMD;",
			
			"Reqs:\n> "//Warhead fragmentation II
			+ specialTechnologyTitles[SpecialTechnology.WARHEAD_FRAGMENTATION_I.ordinal()] + "\n"
			+ "> ACC: 35%\n"
			+ "> SPD: 40%\n"
			+ "\"HIGH JACK THIS FAGS\";",
			
			"Reqs:\n> "//Decoy flares
			+ specialTechnologyTitles[SpecialTechnology.RADIO.ordinal()] + "\n> "
			+ specialTechnologyTitles[SpecialTechnology.STRATEGIC_WARFARE.ordinal()] + "\n"
			+ "> ENG: 50%\n"
			+ "Allows missiles to use decoy flares;",
	};

	@SuppressWarnings("rawtypes")
	public static <E extends Enum, S> S get(E e, S[] s){
		return s[e.ordinal()];
	}
}
