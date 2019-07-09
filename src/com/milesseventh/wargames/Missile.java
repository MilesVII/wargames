package com.milesseventh.wargames;

import java.util.ArrayList;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.milesseventh.wargames.Heartstrings.SpecialTechnology;

public class Missile {
	public enum State{
		UNMOUNTED, READY
	}
	public static final int WEIGHT = Math.round(Utils.remap(.5f, 0, 1, Unit.MIN_CARGO, Unit.MAX_CARGO));
	
	private float[] tech;
	private ArrayList<SpecialTechnology> st;
	private float readiness = 0;
	public State orderedState = State.UNMOUNTED;
	public String name;
	
	private Vector2 from, to;
	public Vector2 position = new Vector2();
	private float traveled = 0, trajectory = -1;
	public boolean exploded = false;
	
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
	
	public void setLaunchData(Vector2 nfrom, Vector2 nto){
		from = nfrom.cpy();
		to = nto.cpy();
		trajectory = Structure.getMissileTrajectory(from, to);
		position = from.cpy();
	}
	
	public void move(float dt){
		assert(from != null);
		assert(to != null);
		assert(trajectory != -1);
		
		traveled += dt * Heartstrings.MISSILE_SPEED;
		float progress = traveled / trajectory;
		
		if (progress >= 1f){
			explode();
		} else {
			float projected = (float)Math.cos(MathUtils.lerp((float)Math.PI, 0, progress));
			projected = Utils.remap(projected, -1, 1, 0, 1);
			position.set(Utils.getVector(to).sub(from).scl(projected).add(from));
		}
	}
	
	private void explode(){
		WG.antistatic.gui.sr.circle(WG.antistatic.getUIFromWorldX(to.x), WG.antistatic.getUIFromWorldY(to.y), 32);
		//Faction.missilesInAir.remove(this);
		exploded = true;
	}
	
}
