package com.milesseventh.wargames;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

public class Fraction {
	public float[] tech         = {0, 0, 0, 0, 0, 0};
	public int[] techPriorities = {1, 1, 1, 1, 1, 1};
	public static final int MAXPRIOR = 100;
	public float investigationBudget = 0;
	public static final float INVESTIGATION_PER_FRAME = .2f;
	public enum Technology{
		FIREPOWER, ARMOR, ACCURACY, SPEED, CARGO, ENGINEERING
	}
	public static String[] technologyTitles = {
		"Firepower", "Armor", "Accuracy", "Speed", "Cargo load", "Engineering"
	};
	
	public ArrayList<SpecialTechnology> specTech = new ArrayList<SpecialTechnology>();
	public enum SpecialTechnology{
		BASIC_WARFARE,          //Allows building of fighters, ammo crafting; Req: ENG(5%)
		COLUMN_INTERCEPTION,    //Allows squads to intercept other squads; Req: BASIC_WARFARE, ACC(20%)
		SIEGE_I, SIEGE_II,      //Allows squads to siege structures and conquer them; Req: BASIC_WARFARE, ARMOR(10%), ARMOR(40%), ACC(10%)
		FORTIFICATION,          //Allows squads to fortify position and defend a spot, acting like a portable MB; Req: BASIC_WARFARE, FP(10%), ARMOR(30%)
		MOBILE_ATTACK,          //Allows squads to attack other squads; Req: BASIC_WARFARE, ACC(5%)
		ADVANCED_WARFARE,       //Allows building of MB; Req: BASIC_WARFARE, ENG(10%), FP(30%)
		RADIO,                  //Allows building of radars; Req: ADVANCED_WARFARE, ENG(15%)
		AMD_I, AMD_II,          //Allows building of AMD stations; Req_1: ADVANCED_WARFARE, ENG(25%), ACC(30%); Allows building of laser AMD stations; Req_2: AMD_I, ENG(45%), ACC(60%) 
		ESPIONAGE,              //Allows fraction to steal foreign special tecnologies by capturing enemy's radars; Req: RADIO, SIEGE, ENG(30%)
		STRATEGIC_WARFARE,      //Allows building of missile silos and missile crafting; Req: ADVANCED_WARFARE, ENG(40%), ACC(25%), FP(50%), SPD(25%)
		WARHEAD_FRAGMENTATION_I,//Allows missiles' payload to fragmentate, increasing effective area and reducing chance to be shotdown by AMD; Req: SW, ENG(60%), FP(60%)
		WARHEAD_FRAGMENTATION_II,//Req: WF_I, ACC(35%), SPD(40%)
		FLARES,                 //Allows missiles to use decoy flares; Req: SW, RADIO, ENG(50%)
	}
	public static String[] specialTechnologyTitles = {
			"Basic warfare", "Column interception", "Siege I", "Siege II", "Fortification", "Column attack", 
			"Advanced warfare", "Electronic warfare", "Anti-Missile Defence I", "Anti-Missile Defence II", 
			"Industrial espionage", "Strategic warfare", "Warhead fragmentation I", "Warhead fragmentation II", "Decoy flares"
		};
	public static String[] specialTechnologyPrompts = {
			"Reqs:\n"//Basic warfare
			+ "> ENG: 5%\n"
			+ "Allows building of fighters, ammo crafting;", 
			
			"Reqs:\n> "//Column interception
			+ specialTechnologyTitles[SpecialTechnology.BASIC_WARFARE.ordinal()] + "\n"
			+ "> ACC: 20%\n"
			+ "Allows squads to intercept other squads;", 
			
			"Reqs:\n> "//Siege I
			+ specialTechnologyTitles[SpecialTechnology.BASIC_WARFARE.ordinal()] + "\n"
			+ "> ARM: 10%\n"
			+ "> ACC: 10%\n"
			+ "Allows squads to siege structures and capture them;", 
			
			"Reqs:\n> "//Siege II
			+ specialTechnologyTitles[SpecialTechnology.BASIC_WARFARE.ordinal()] + "\n"
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
			+ "Allows missiles' payload to fragmentate, increasing "
			+ "effective area and reducing chance to be shotdown by AMD;",
			
			"Reqs:\n> "//Warhead fragmentation II
			+ specialTechnologyTitles[SpecialTechnology.WARHEAD_FRAGMENTATION_I.ordinal()] + "\n"
			+ "> ACC: 35%\n"
			+ "> SPD: 40%\n"
			+ "Allows missiles' payload to fragmentate, increasing "
			+ "effective area and reducing chance to be shotdown by AMD;",
			
			"Reqs:\n> "//Decoy flares
			+ specialTechnologyTitles[SpecialTechnology.RADIO.ordinal()] + "\n> "
			+ specialTechnologyTitles[SpecialTechnology.STRATEGIC_WARFARE.ordinal()] + "\n"
			+ "> ENG: 50%\n"
			+ "Allows missiles to use decoy flares;",
		};
	
	
	private String name;
	private Color fractionColor;
	private ArrayList<Structure> structs = new ArrayList<Structure>();
	private Structure capital;
	
	public Fraction (Color _color, String _name, Vector2 _pos){
		name = _name;
		fractionColor = _color;
		
		capital = new Structure(_pos, Structure.StructureType.CITY, this);
		structs.add(capital);
	}
	
	public boolean isInvestigated(SpecialTechnology st){
		return specTech.contains(st);
	}
	
	public float techLevel(Technology t){
		return tech[t.ordinal()];
	}
	
	public boolean isInvestigationAllowed(SpecialTechnology st){
		switch(st){
		case BASIC_WARFARE: 
			return (techLevel(Technology.ENGINEERING) > .05f);
		case COLUMN_INTERCEPTION: 
			return (isInvestigated(SpecialTechnology.BASIC_WARFARE)           && techLevel(Technology.ACCURACY)    > .2f);
		case SIEGE_I: 
			return (isInvestigated(SpecialTechnology.BASIC_WARFARE)           && techLevel(Technology.ARMOR)       > .1f    && techLevel(Technology.ACCURACY)    > .1f);
		case SIEGE_II: 
			return (isInvestigated(SpecialTechnology.BASIC_WARFARE)           && techLevel(Technology.ARMOR)       > .4f);
		case FORTIFICATION: 
			return (isInvestigated(SpecialTechnology.BASIC_WARFARE)           && techLevel(Technology.FIREPOWER)   > .1f    && techLevel(Technology.ARMOR)       > .3f);
		case MOBILE_ATTACK: 
			return (isInvestigated(SpecialTechnology.BASIC_WARFARE)           && techLevel(Technology.ACCURACY)    > .05f);
		case ADVANCED_WARFARE:
			return (isInvestigated(SpecialTechnology.BASIC_WARFARE)           && techLevel(Technology.ENGINEERING) > .1f    && techLevel(Technology.FIREPOWER)   > .3f);
		case RADIO:
			return (isInvestigated(SpecialTechnology.ADVANCED_WARFARE)        && techLevel(Technology.ENGINEERING) > .15f);
		case AMD_I:
			return (isInvestigated(SpecialTechnology.ADVANCED_WARFARE)        && techLevel(Technology.ACCURACY)    > .30f   && techLevel(Technology.ENGINEERING) > .25f);
		case AMD_II:
			return (isInvestigated(SpecialTechnology.AMD_I)                   && isInvestigated(SpecialTechnology.RADIO)    && techLevel(Technology.ACCURACY)    > .60f   && techLevel(Technology.ENGINEERING) > .45f);
		case ESPIONAGE:
			return (isInvestigated(SpecialTechnology.RADIO)                   && isInvestigated(SpecialTechnology.SIEGE_II) && techLevel(Technology.ENGINEERING) > .3f);
		case STRATEGIC_WARFARE:
			return (isInvestigated(SpecialTechnology.ADVANCED_WARFARE)        && techLevel(Technology.ENGINEERING) > .4f    && techLevel(Technology.ACCURACY)    > .25f && techLevel(Technology.FIREPOWER) > .5f && techLevel(Technology.SPEED) > .25f);
		case WARHEAD_FRAGMENTATION_I:
			return (isInvestigated(SpecialTechnology.STRATEGIC_WARFARE)       && techLevel(Technology.ENGINEERING) > .6f    && techLevel(Technology.FIREPOWER)   > .6f);
		case WARHEAD_FRAGMENTATION_II:
			return (isInvestigated(SpecialTechnology.WARHEAD_FRAGMENTATION_I) && techLevel(Technology.ACCURACY)    > .35f   && techLevel(Technology.SPEED)       > .4f);
		case FLARES:
			return (isInvestigated(SpecialTechnology.STRATEGIC_WARFARE)       && isInvestigated(SpecialTechnology.RADIO)    && techLevel(Technology.ENGINEERING) > .5f);
		}
		return false;
	}
	
	public void doInvestigation(){
		int prioSum = 0;
		for (int i = 0; i < techPriorities.length; i++){
			if (tech[i] >= 1){
				tech[i] = 1;
				techPriorities[i] = 0;
			}
			prioSum += techPriorities[i];
		}
		if (prioSum == 0)
			return;
		float budget = Math.min(INVESTIGATION_PER_FRAME, investigationBudget);
		investigationBudget -= budget;
		for (int i = 0; i < tech.length; i++)
			tech[i] += (techPriorities[i] / (float) prioSum) * budget / 1000f;
	}
	
	public void unregisterStructure(Structure _victim){
		structs.remove(_victim);
	}
	
	public void registerStructure(Structure _victim){
		structs.add(_victim);
	}
	
	public ArrayList<Structure> getStructs(){
		return structs;
	}
	
	public Color getColor(){
		return fractionColor;
	}
	
	public Structure getCapital(){
		return capital;
	}
}
