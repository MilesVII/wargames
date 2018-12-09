package com.milesseventh.wargames;

import java.util.ArrayList;
import java.util.Random;

import com.badlogic.gdx.math.MathUtils;
import com.milesseventh.wargames.Heartstrings.SpecialTechnology;
import com.milesseventh.wargames.Heartstrings.Technology;

public class Unit {
	public static final String[] FIGHTER_NAMES = {"Dreadnought","Silly","Arion","Hawkeye","Zeroer","Equalizer","Shader","Humming","Grim","Scarlet","Tranquillity","The Climax","Bloodfury","Trinity","Whisper","Stormbringer","LightBringer","Nightmare","Courier","The Collapse","Wrong","Eater","Eternal","Backfire","Burnout","Collider","Horizon","Acheron","Smashine","Greedy","Swift","Project-71"};
	public static Random r = new Random();

	public enum Type {FIGHTER, TRANSPORTER, BUILDER};
	public enum State {PARKED, REPAIRING, UPGRADING, ACTIVE, FORTIFIED};
	
	public static final float MAX_CARGO = 1;
	public static final float[] MAX_CONDITIONS_NO_ARM_TECH  = {700, 120, 320};
	public static final float[] MAX_CONDITIONS_MAX_ARM_TECH = {1500, 750, 520};
	
	public String name;
	public float[] techLevel;
	public ArrayList<SpecialTechnology> st = new ArrayList<SpecialTechnology>();
	public Faction owner;
	public Type type;
	public State state;
	public Structure manufacturer;
	public float condition;
	
	public float upgradeTime;
	
	@SuppressWarnings("unchecked")
	public Unit(Structure nmanufacturer, Type ntype, float[] ntech, ArrayList<SpecialTechnology> nst) {
		manufacturer = nmanufacturer;
		owner = manufacturer.ownerFaction;
		type = ntype;
		techLevel = ntech.clone();
		st = (ArrayList<SpecialTechnology>)nst.clone();
		condition = getMaxCondition();
		
		switch (type){
		case FIGHTER:
			name = "MFV-" + r.nextInt(128) + " \"" + FIGHTER_NAMES[r.nextInt(FIGHTER_NAMES.length)] + "\"";
			break;
		case TRANSPORTER:
			name = "MT-" + r.nextInt(256);
			break;
		case BUILDER:
			name = "MCV #" + r.nextInt(32);
			break;
		}
		state = State.PARKED;
	}
	
	public void setTechLevel(Technology t, float in){
		techLevel[t.ordinal()] = MathUtils.clamp(in, 0, 1);
	}
	
	public float getMaxCondition(){
		return MathUtils.lerp(MAX_CONDITIONS_NO_ARM_TECH[type.ordinal()], 
		                      MAX_CONDITIONS_MAX_ARM_TECH[type.ordinal()], 
		                      techLevel[Technology.ARMOR.ordinal()]);
	}
	
	public boolean isDamaged(){
		return condition < getMaxCondition() * .99f;
	}
	
	public boolean canBeRepaired(Structure operator){
		return operator.getResource(Structure.Resource.METAL) >= Heartstrings.getRepairCostInMetal(this);
	}
}
