package com.milesseventh.wargames;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

public class GUI {
	private static final Color[] GUI_BUTTON_CLOSE_COLORS = {
		new Color(.75f, 0, 0, .8f), 
		new Color(.75f, 0, 0, 1), 
		new Color(.64f, 0, 0, 1)
	};

	private WG context;
	
	public GUI(WG _context) {
		context = _context;
	}
	
	public void button(ShapeRenderer sr, Vector2 position, Vector2 size, Runnable callback, Color[] colors){
		Color color = colors[0];
		if (Utils.UIMousePosition.x > position.x && Utils.UIMousePosition.x < position.x + size.x &&
			Utils.UIMousePosition.y > position.y && Utils.UIMousePosition.y < position.y + size.y){
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
			float angle = Utils.getAngle(context.getUIFromWorldV(Utils.WorldMousePosition).sub(position));
			if (angle > i * (360 / (float) actions.length) + PIE_MENU_SECTOR_MARGIN &&
			    angle < (i + 1) * (360 / (float) actions.length) + PIE_MENU_SECTOR_MARGIN){
				if (Utils.isTouchJustReleased)
					actions[i].run();
				sr.setColor(selected);
			} else
				sr.setColor(unselected);
			Utils.drawTrueArc(sr, position, 20, i * (360 / (float) actions.length) + PIE_MENU_SECTOR_MARGIN, (360 / (float) actions.length) - 2 * PIE_MENU_SECTOR_MARGIN, 70);
		}
	}
	
	public void dialog(WG.Dialog dialog, ShapeRenderer sr){
		sr.setColor(WG.GUI_DIALOG_BGD);
		sr.rect(0, (WG.UI_H - WG.DIALOG_HEIGHT * WG.UI_H) / 2, WG.UI_W, WG.DIALOG_HEIGHT * WG.UI_H);
		button(sr, WG.GUI_BUTTON_POS_CLOSE, WG.GUI_BUTTON_SIZ_CLOSE, WG.GUI_BUTTON_ACT_CLOSE, GUI_BUTTON_CLOSE_COLORS);
		switch (dialog){
		case UNITS_BUILDING:
			break;
		case RESOURCE_MANAGER:
			break;
		case UNITS_ASSEMBLY:
			break;
		default:
			break;
		}
	}
	
	public void 
}
