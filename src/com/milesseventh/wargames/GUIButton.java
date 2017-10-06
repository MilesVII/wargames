package com.milesseventh.wargames;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

public class GUIButton {
	/*
	 * WORKS BUT PRESERVED
	 */
	public interface GUIEvents{
		public void action(Object sender);
		public void actionContinious(Object sender);
	}
	
	public Vector2 position, size;
	private static Color DEFAULT_COLOR = new Color(0, 0, 0, .5f), 
						HOVER_COLOR = new Color(0, 0, 0, 1), 
						CLICK_COLOR = new Color(.5f, .5f, .5f, .8f);
	private Color color;
	private GUIEvents callback;
	
	public GUIButton(Vector2 _position, Vector2 _size, GUIEvents _callmebackpls) {
		position = _position;
		size = _size;
		callback = _callmebackpls;
	}

	public void render(ShapeRenderer _sr){
		color = DEFAULT_COLOR;
		if (Utils.HUDMousePosition.x > position.x && Utils.HUDMousePosition.x < position.x + size.x &&
			Utils.HUDMousePosition.y > position.y && Utils.HUDMousePosition.y < position.y + size.y){
			color = HOVER_COLOR;
			if (Gdx.input.isTouched()){
				callback.actionContinious(this);
				color = CLICK_COLOR;
			}
			if (Gdx.input.justTouched())
				callback.action(this);
		}
		_sr.setColor(color);
		_sr.rect(position.x, position.y, size.x, size.y);
	}
}