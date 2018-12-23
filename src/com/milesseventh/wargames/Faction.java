package com.milesseventh.wargames;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Queue;
import com.milesseventh.wargames.Heartstrings.Craftable;
import com.milesseventh.wargames.Heartstrings.SpecialTechnology;
import com.milesseventh.wargames.Heartstrings.Technology;
import com.milesseventh.wargames.properties.SpecialTechnologyProperties.TechnologyRequirement;

public class Faction {
	public static ArrayList<Faction> factions = new ArrayList<Faction>();
	
	public static Faction debug;
	
	public static Texture[] ICONS = {
			new Texture(Gdx.files.internal("city.png")),
			new Texture(Gdx.files.internal("mine.png")),
			new Texture(Gdx.files.internal("mine.png")),
			new Texture(Gdx.files.internal("mine.png")),
			new Texture(Gdx.files.internal("mine.png")),
			new Texture(Gdx.files.internal("mine.png")),

			new Texture(Gdx.files.internal("capital.png"))
	};
	public static final Texture SQUAD_ICON = new Texture(Gdx.files.internal("squad.png"));
	
	public float[] tech         = {1, 1, 1, 1, 1, 1};//{0, 0, 0, 0, 0, 0};
	public int[] techPriorities = {0, 0, 0, 0, 0, 0};
	public static final int MAXPRIOR = 100;
	public ArrayList<Craftable> availableCraftables = new ArrayList<Craftable>();
	public ArrayList<SpecialTechnology> specTech = new ArrayList<SpecialTechnology>();
	private Queue<SpecialTechnology> pendingST = new Queue<SpecialTechnology>();
	private float stInvestigationDone = 0;
	
	public static final float INVESTIGATION_PER_MS = .2f,
	                          ST_INVESTIGATION_PER_MS = .2f;
	
	public String name;
	public Color factionColor;
	public ArrayList<Structure> structs = new ArrayList<Structure>();
	public ArrayList<Squad> squads = new ArrayList<Squad>();
	public Structure capital = null;
	public float scienceDataAvailable = 0;
	public float investition = 0;
	
	public Faction (Color _color, String _name, Vector2 _pos){
		name = _name;
		factionColor = _color;
		
		availableCraftables.add(Craftable.SCIENCE);
		availableCraftables.add(Craftable.TRANSPORTER);
		availableCraftables.add(Craftable.BUILDER);
		capital = new Structure(_pos, Structure.Type.CITY, this);
		structs.add(capital);
		capital.resources.add(Resource.ORE, 100);
		capital.resources.add(Resource.METAL, 1000);
		capital.resources.add(Resource.OIL, 2000);
		capital.resources.add(Resource.FUEL, 20000);
		capital.resources.add(Resource.AMMO, 10000);
		scienceDataAvailable = 70000;
		
		debug = this;

		factions.add(this);
	}
	
	public void update(float dt){
		doInvestigation(dt);
		for (Structure s : structs)
			s.update(dt);
		for (Squad s : squads)
			s.update(dt);
	}
	
	public boolean isInvestigated(SpecialTechnology st){
		return specTech.contains(st);
	}
	
	public boolean isBeingInvestigated(SpecialTechnology st){
		for (int i = 0; i < pendingST.size; ++i)
			if (pendingST.get(i).equals(st))
				return true;
		return false;
	}
	
	public float techLevel(Technology t){
		return tech[t.ordinal()];
	}
	
	public float getRelativeInvestigationPriority(Technology t){
		if (tech[t.ordinal()] == 1f)
			return 0;
		int sum = 0;
		for (int i = 0; i < tech.length; ++i)
			if (tech[i] != 1f)
				sum += techPriorities[i];
		
		if (sum == 0)
			return 0f;
		return techPriorities[t.ordinal()] / (float) sum;
	}
	
	public boolean isSTInvestigationPossibleRightNow(int st){
		SpecialTechnology s = Heartstrings.SpecialTechnology.values()[st];
		//Check if ST is already investigated/ing
		if (isInvestigated(s))
			return false;
		if (isBeingInvestigated(s))
			return false;
		//Check if technology is developed
		for (TechnologyRequirement tr: Heartstrings.stProperties[st].techReqs)
			if (techLevel(tr.tech) < tr.level)
				return false;
		
		//Check if there is enough science data stored
		return Heartstrings.stProperties[st].investigationPriceInData <= scienceDataAvailable;
	}
	
	public void startInvestigatingSpecialTechnology(int st){
		scienceDataAvailable -= Heartstrings.stProperties[st].investigationPriceInData;
		pendingST.addLast(SpecialTechnology.values()[st]);
	}
	
	public void onSpecialTechnologyInvestigated(int st){
		onSpecialTechnologyInvestigated(SpecialTechnology.values()[st]);
	}
	private void onSpecialTechnologyInvestigated(SpecialTechnology st){
		specTech.add(st);
		if (st.equals(SpecialTechnology.BASIC_WARFARE)){
			availableCraftables.add(Craftable.FIGHTER);
			availableCraftables.add(Craftable.AMMO);
		} else if (st.equals(SpecialTechnology.STRATEGIC_WARFARE)){
			availableCraftables.add(Craftable.MISSILE);
		}
		tempCraftTitles = null;//Used to rebuild array
	}
	
	public void doInvestigation(float dt){
		if (pendingST.size > 0){
			stInvestigationDone += dt * ST_INVESTIGATION_PER_MS / 
			                       Heartstrings.get(pendingST.first(), Heartstrings.stProperties).investigationWorkamount;
			if (stInvestigationDone >= 1){
				stInvestigationDone = 0;
				specTech.add(pendingST.first());
				onSpecialTechnologyInvestigated(pendingST.first());
				pendingST.removeFirst();
			}
		}
		int prioSum = getPrioSum();
		if (prioSum == 0)
			return;
		float budget = Math.min(INVESTIGATION_PER_MS * investition * dt, scienceDataAvailable);
		scienceDataAvailable -= budget;
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
				tempCraftTitles[i] = Heartstrings.get(availableCraftables.get(i), Heartstrings.craftableProperties).title;//craftableTitles[availableCraftables.get(i).ordinal()];
		}
		return tempCraftTitles;
	}
	
	public void unregisterStructure(Structure _victim){
		structs.remove(_victim);
	}
	
	public void registerStructure(Structure _victim){
		structs.add(_victim);
	}
}
