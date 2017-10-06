package com.milesseventh.wargames;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

public class GUI {
	public GUI() {}
	public void button(ShapeRenderer sr, Vector2 position, Vector2 size, Runnable callback, Color[] colors){
		Color color = colors[0];
		if (Utils.HUDMousePosition.x > position.x && Utils.HUDMousePosition.x < position.x + size.x &&
			Utils.HUDMousePosition.y > position.y && Utils.HUDMousePosition.y < position.y + size.y){
			color = colors[1];
			if (Gdx.input.isTouched())
				color = colors[2];
			if (Gdx.input.justTouched())
				callback.run();
		}
		sr.setColor(color);
		sr.rect(position.x, position.y, size.x, size.y);
	}
}
