package com.milesseventh.wargames;

import java.util.ArrayList;

import com.badlogic.gdx.math.Vector2;

public interface Piemenuable {
	public Vector2 getWorldPosition();
	public ArrayList<PiemenuEntry> getEntries();
}
