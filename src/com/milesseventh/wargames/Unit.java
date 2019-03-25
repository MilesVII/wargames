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

	public static final float MIN_CARGO = 120f;
	public static final float MAX_CARGO = 700f;
	
	public String name;
	public float[] techLevel;
	public ArrayList<SpecialTechnology> st = new ArrayList<SpecialTechnology>();
	public ArrayList<Missile> missilesLoaded = new ArrayList<Missile>();
	public ResourceStorage resources;
	public Faction faction;
	public Type type;
	public State state;
	public Structure manufacturer;
	public float condition;
	
	public float upgradeTime;
	
	@SuppressWarnings("unchecked")
	public Unit(Structure nmanufacturer, Type ntype, float[] ntech, ArrayList<SpecialTechnology> nst) {
		manufacturer = nmanufacturer;
		faction = manufacturer.faction;
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
		resources = new ResourceStorage(name);
		state = State.PARKED;
	}
	
	public void setTechLevel(Technology t, float in){
		techLevel[t.ordinal()] = MathUtils.clamp(in, 0, 1);
	}
	
	public float getSpeed(){
		return Utils.remap(Heartstrings.get(Technology.SPEED, techLevel), 0, 1,
		                   Heartstrings.get(type, Heartstrings.uProperties).minSpeed,
		                   Heartstrings.get(type, Heartstrings.uProperties).maxSpeed);
	}
	
	public float getCapacity(){
		if (type == Type.TRANSPORTER)
			return Utils.remap(techLevel[Technology.CARGO.ordinal()], 0, 1, MIN_CARGO, MAX_CARGO);
		else
			return 0;
	}
	
	public float getFreeSpace(){
		return getCapacity() - resources.sum() - missilesLoaded.size() * Missile.WEIGHT;
	}
	
	public int getMissilesFreeSpace(){
		if (type == Type.TRANSPORTER)
			return (int)Math.floor(getFreeSpace() / Missile.WEIGHT);//TODO: FOX THE MISSILE CAPACITY CHECK ASAP+ missilesLoaded.size();
		else
			return 0;
	}
	
	public float getFuelConsumption(){
		return Heartstrings.get(type, Heartstrings.uProperties).fuelConsumption * Utils.remap(techLevel[Technology.SPEED.ordinal()], 0, 1, 1, .3f);
	}
	
	public float getMaxCondition(){
		return MathUtils.lerp(Heartstrings.get(type, Heartstrings.uProperties).minMaxCondition, 
		                      Heartstrings.get(type, Heartstrings.uProperties).maxMaxCondition, 
		                      techLevel[Technology.ARMOR.ordinal()]);
	}
	
	public float getAttackRange2(){
		float r = MathUtils.lerp(Heartstrings.SQUAD_ATTACK_RANGE_MIN, Heartstrings.SQUAD_ATTACK_RANGE_MAX, techLevel[Technology.ACCURACY.ordinal()]);
		return r * r;
	}
	
	public float getFirepower(){
		float r = MathUtils.lerp(Heartstrings.SQUAD_ATTACK_RANGE_MIN, Heartstrings.SQUAD_ATTACK_RANGE_MAX, techLevel[Technology.FIREPOWER.ordinal()]);
		return r * r;
	}
	
	public boolean isDamaged(){
		return condition < getMaxCondition() * .99f;
	}
	
	public boolean canBeRepaired(Structure operator){
		return operator.resources.get(Resource.METAL) >= Heartstrings.getRepairCostInMetal(this);
	}
	
	public float receiveDamage(float power){
		float debrisDamage = power - condition;
		condition -= power;
		return debrisDamage * .7f;
	}
}
