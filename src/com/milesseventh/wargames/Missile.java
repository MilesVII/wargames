package com.milesseventh.wargames;

import java.util.ArrayList;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.milesseventh.wargames.Heartstrings.SpecialTechnology;
import com.milesseventh.wargames.Heartstrings.Technology;

public class Missile {
	public enum State{
		UNMOUNTED, READY
	}
	public static final int WEIGHT = Math.round(Utils.remap(.5f, 0, 1, Unit.MIN_CARGO, Unit.MAX_CARGO));
	
	public float[] tech;
	private ArrayList<SpecialTechnology> st;
	private float readiness = 0;
	public State orderedState = State.UNMOUNTED;
	public String name;
	
	private Vector2 from, to;
	public Vector2 position = new Vector2();
	private float traveled = 0, trajectory = -1;
	public boolean exploded = false;
	public boolean canFragmentate = false;
	public boolean fragmentated = false;
	public float damageReducer = 1f;
	
	@SuppressWarnings("unchecked")
	public Missile(float[] nt, ArrayList<SpecialTechnology> nst) {
		tech = nt.clone();
		st = (ArrayList<SpecialTechnology>)nst.clone();
		canFragmentate = st.contains(SpecialTechnology.WARHEAD_FRAGMENTATION);
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
	
	public void setLaunchData(Vector2 nfrom, Vector2 nto, boolean enableShake){
		from = nfrom.cpy();
		
		to = nto.cpy();
		if (enableShake){
			float shake = MathUtils.lerp(Heartstrings.MISSILE_SHAKE_MAX, 0, Heartstrings.get(Technology.ACCURACY, tech));
			Utils.shakePoint(to, shake);
		}
		
		trajectory = Structure.getMissileTrajectory(from, to);
		position = from.cpy();
	}
	
	public void move(float dt){
		assert(from != null);
		assert(to != null);
		assert(trajectory != -1);
		
		float speed = MathUtils.lerp(Heartstrings.MISSILE_SPEED_MIN, Heartstrings.MISSILE_SPEED_MAX, Heartstrings.get(Technology.SPEED, tech));
		traveled += dt * speed;
		float progress = traveled / trajectory;
		
		if (canFragmentate && !fragmentated && progress >= .5f){
			progress = .5f;
			traveled = trajectory * 5f;
			fragmentate();
		}
		
		if (progress >= 1f){
			explode();
		} else {
			float projected = (float)Math.cos(MathUtils.lerp((float)Math.PI, 0, progress));
			projected = Utils.remap(projected, -1, 1, 0, 1);
			position.set(Utils.getVector(to).sub(from).scl(projected).add(from));
		}
	}
	
	public float getBlastRadius(){
		return MathUtils.lerp(Heartstrings.MISSILE_BLAST_RADIUS_MIN, Heartstrings.MISSILE_BLAST_RADIUS_MAX, Heartstrings.get(Technology.FIREPOWER, tech));
	}
	
	public float getTotalDestructionRadius(){
		return getBlastRadius() * Heartstrings.MISSILE_BLAST_CORE_FRACTION;
	}
	
	public int getFuelNeeded(Vector2 from, Vector2 target){
		float trajectory = Structure.getMissileTrajectory(from, target);
		float discounted = 1 - MathUtils.lerp(0, Heartstrings.MISSILE_FUEL_CONSUMPTION_DISCOUNT_MAX, Heartstrings.get(Technology.SPEED, tech));
		return Math.round(trajectory * Heartstrings.MISSILE_FUEL_CONSUMPTION_RELATIVE * discounted);
	}
	
	private void fragmentate(){
		int fragments = Heartstrings.MISSILE_FRAGMENTS_COUNT;
		for (int i = 0; i < fragments - 1; ++i){
			//TODO: try to make it more readable
			Missile m = new Missile(tech, st);
			Vector2 offed = Utils.getVector(to);
			Utils.shakePoint(offed, getTotalDestructionRadius() * .9f);
			Vector2 ghostFrom = Utils.getVector(position).sub(Utils.getVector(offed).sub(position));
			m.setLaunchData(ghostFrom, offed, false);
			m.traveled = m.trajectory * .5f;
			m.damageReducer = damageReducer / (float)Heartstrings.MISSILE_FRAGMENTS_COUNT;
			m.fragmentated = true;
			m.move(0);
			Faction.missilesInAir.add(m);
		}
		Utils.shakePoint(to, getTotalDestructionRadius() * .9f);
		damageReducer /= (float)Heartstrings.MISSILE_FRAGMENTS_COUNT;
		fragmentated = true;
	}
	
	private void explode(){
		//Cause damage
		float blastRadius = getBlastRadius();
		float totalDestructionRadius = getTotalDestructionRadius();
		float maxDamage = MathUtils.lerp(Heartstrings.MISSILE_EXPLOSION_DAMAGE_MIN, Heartstrings.MISSILE_EXPLOSION_DAMAGE_MAX, Heartstrings.get(Technology.FIREPOWER, tech));
		
		for (Container c: Faction.containers)
			explode(c, blastRadius, totalDestructionRadius, maxDamage);
		for (Faction f: Faction.factions){
			for (Squad s: f.squads)
				explode(s, blastRadius, totalDestructionRadius, maxDamage);
			for (Structure s: f.structs)
				explode(s, blastRadius, totalDestructionRadius, maxDamage);
		}
		
		//Initiate drawing effects
		WG.antistatic.gui.sr.circle(WG.antistatic.getUIFromWorldX(to.x), WG.antistatic.getUIFromWorldY(to.y), blastRadius);
		
		//End missile flight
		//Faction.missilesInAir.remove(this);
		exploded = true;
	}
	
	private void explode(Object target, float blastRadius, float totalDestructionRadius, float maxDamage){
		Vector2 targetPosition;
		boolean isCombatant = target instanceof Combatant;
		
		if (isCombatant)
			targetPosition = ((Combatant)target).getPosition();
		else {
			assert(target instanceof Container);
			targetPosition = ((Container)target).position;
		}
		
		//Erase targets with all cargo that was too close to explosion center
		//Or cause damage on more distant ones
		float targetDistance = to.dst(targetPosition);
		if (targetDistance <= totalDestructionRadius){
			if (target instanceof Container)
				((Container)target).lifetimeInSeconds = -1;
			else if (target instanceof Squad)
				((Squad)target).destroyed = true;
			else if (target instanceof Structure)
				((Structure)target).destroyed = true;
			else
				assert(false);
		} else if (targetDistance <= blastRadius && isCombatant){
			//float damage = MathUtils.lerp(maxDamage, 0, (targetDistance - totalDestructionRadius) / (blastRadius - totalDestructionRadius));
			float damage = Utils.remap(targetDistance, totalDestructionRadius, blastRadius, maxDamage, 0f);
			damage *= damageReducer;
			((Combatant)target).receiveFire(damage);
		}
	}
}
