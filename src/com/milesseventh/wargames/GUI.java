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
	
	private static final int PIE_MENU_SECTOR_MARGIN = 5;
	public void piemenu(ShapeRenderer sr, Vector2 position, float radius, Color unselected, Color selected, Runnable[] actions){
		for(int i = 0; i < actions.length; i++){
			float angle = Utils.getAngle(WG.antistatic.getHUDFromWorldV(Utils.WorldMousePosition).sub(position));
			if (angle > i * (360 / (float) actions.length) + PIE_MENU_SECTOR_MARGIN &&
			    angle < (i + 1) * (360 / (float) actions.length) + PIE_MENU_SECTOR_MARGIN){
				if (Utils.isTouchJustReleased)
					actions[i].run();
				sr.setColor(selected);
			} else
				sr.setColor(unselected);
			Utils.drawTrueArc(sr, position, 20, i * (360 / (float) actions.length) + PIE_MENU_SECTOR_MARGIN, (360 / (float) actions.length) - 2 * PIE_MENU_SECTOR_MARGIN, 70);
			//sr.arc(position.x, position.y, radius, i * (360 / (float) actions.length) + PIE_MENU_SECTOR_MARGIN, (360 / (float) actions.length) - 2 * PIE_MENU_SECTOR_MARGIN, 70);
		}
	}
}
