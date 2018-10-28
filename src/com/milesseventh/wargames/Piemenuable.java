package com.milesseventh.wargames;

import com.badlogic.gdx.math.Vector2;

public interface Piemenuable {
	public Vector2 getWorldPosition();
	public int getActionsAmount();
	public Callback getAction();
}
