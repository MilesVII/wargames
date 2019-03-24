package com.milesseventh.wargames;

import com.badlogic.gdx.math.Vector2;

public interface Combatant {
	public void receiveFire(float power);
	public Vector2 getPosition();
}
