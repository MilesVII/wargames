package com.milesseventh.wargames;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.milesseventh.wargames.Heartstrings.Craftable;
import com.milesseventh.wargames.Heartstrings.SpecialTechnology;
import com.milesseventh.wargames.Heartstrings.Technology;

public class Fraction {
	public static Fraction debug;
	public float[] tech         = {0, 0, 0, 0, 0, 0};
	public int[] techPriorities = {1, 1, 1, 1, 1, 1};
	public static final int MAXPRIOR = 100;
	public float investigationBudget = 0;
	public static final float INVESTIGATION_PER_FRAME = 7f;//.2f;
	public ArrayList<Craftable> availableCraftables = new ArrayList<Craftable>();
	private ArrayList<SpecialTechnology> specTech = new ArrayList<SpecialTechnology>();
	
	
	private String name;
	private Color fractionColor;
	private ArrayList<Structure> structs = new ArrayList<Structure>();
	private Structure capital;
	
	public Fraction (Color _color, String _name, Vector2 _pos){
		debug = this;
		name = _name;
		fractionColor = _color;
		availableCraftables.add(Craftable.SCIENCE);
		availableCraftables.add(Craftable.TRANSPORTER);
		availableCraftables.add(Craftable.BUILDER);
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
			return (isInvestigated(SpecialTechnology.SIEGE_I)                 && techLevel(Technology.ARMOR)       > .4f);
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
	

	public void investigateSpecialTechnology(int st){
		try{
			investigateSpecialTechnology(SpecialTechnology.values()[st]);
			if (WG.antistatic.gui.cd != null)
				WG.antistatic.gui.cd.generateAvailableSTBySelected();
		} catch (ArrayIndexOutOfBoundsException e){
			e.printStackTrace();
		}
	}
	public void investigateSpecialTechnology(SpecialTechnology st){
		if (isInvestigationAllowed(st) && !specTech.contains(st)){
			specTech.add(st);
			if (st.equals(SpecialTechnology.BASIC_WARFARE)){
				availableCraftables.add(Craftable.FIGHTER);
				availableCraftables.add(Craftable.AMMO);
			} else if (st.equals(SpecialTechnology.STRATEGIC_WARFARE)){
				availableCraftables.add(Craftable.MISSILE);
			}
			tempCraftTitles = null;//Used to rebuild array
		}
	}
	
	public void doInvestigation(){
		int prioSum = getPrioSum();
		if (prioSum == 0)
			return;
		float budget = Math.min(INVESTIGATION_PER_FRAME, investigationBudget);
		investigationBudget -= budget;
		for (int i = 0; i < tech.length; i++)
			tech[i] += (techPriorities[i] / (float) prioSum) * budget / 1000f;
	}
	
	public int getPrioSum(){
		int prioSum = 0;
		for (int i = 0; i < techPriorities.length; i++){
			if (tech[i] >= 1){
				tech[i] = 1;
				techPriorities[i] = 0;
			}
			prioSum += techPriorities[i];
		}
		return prioSum;
	}
	
	private String[] tempCraftTitles;
	public String[] getCraftTitles(){
		if (tempCraftTitles == null){
			tempCraftTitles = new String[availableCraftables.size()];
			for (int i = 0; i < tempCraftTitles.length; i++)
				tempCraftTitles[i] = Heartstrings.get(availableCraftables.get(i), Heartstrings.craftableTitles);//craftableTitles[availableCraftables.get(i).ordinal()];
		}
		return tempCraftTitles;
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
