package com.milesseventh.wargames;

import java.util.ArrayList;

import com.badlogic.gdx.math.MathUtils;
import com.milesseventh.wargames.Heartstrings.SpecialTechnology;

public class Missile {
	public enum State{
		UNMOUNTED, READY
	}
	public static final float WEIGHT = Utils.remap(.5f, 0, 1, Unit.MIN_CARGO, Unit.MAX_CARGO);
	
	private float[] tech;
	private ArrayList<SpecialTechnology> st;
	private float readiness = 0;
	public State orderedState = State.UNMOUNTED;
	public String name;
	
	@SuppressWarnings("unchecked")
	public Missile(float[] nt, ArrayList<SpecialTechnology> nst) {
		tech = nt.clone();
		st = (ArrayList<SpecialTechnology>)nst.clone();
		
		name = "Sidewinder";
	}

	public void executeOrder(float delta){
		if (orderedState == State.UNMOUNTED)
			delta *= -1;
		readiness += delta;
		readiness = MathUtils.clamp(readiness, 0, 1);
	}
	
	public boolean isReady(){
		return readiness == 1;
	}
	
	public boolean isUnmounted(){
		return readiness == 0;
	}
	
	public float getReadiness(){
		return readiness;
	}
}
