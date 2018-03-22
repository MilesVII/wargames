package com.milesseventh.wargames;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

public interface ListEntryCallback extends Callback{
	public void entry(Vector2 position, Vector2 size, int id, Color[] color);
}
