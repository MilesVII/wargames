package com.milesseventh.wargames;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

public class GUI {
	private static final Color[] GUI_BUTTON_CLOSE_COLORS = {
			new Color(.75f, 0, 0, .8f), 
			new Color(.75f, 0, 0, 1), 
			new Color(.64f, 0, 0, 1)
		};
	public static final Color[] GUI_SCROLLBAR_COLORS = {
			new Color(.8f, .8f, .8f, .8f), 
			new Color(.64f, .64f, .64f, 1), 
			new Color(.97f, .97f, .97f, 1)
		};
	
	public class UIScrollbar{
		public UIScrollbar(Color[] cs){
			color= cs;
		}
		
		public boolean isActive = false, firstUse = true;
		public int maxStates = -1, offset = 0;
		public Vector2 position, size;
		public Color[] color;//0 -- Bar bgd, 1 -- Bar normal, 2 -- Bar hovered/pressed
	}
	
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
	
	public UIScrollbar initScrollbar(Color[] colorScheme){
		return new UIScrollbar(colorScheme);
	}
	
	private static final int SCROLL_LIST_MARGIN = 2;
	public void scrollableList(ShapeRenderer sr, BitmapFont font, Batch batch,
	                           Vector2 position, Vector2 size, float scrollbarWidth, 
	                           Color[] entryColor, String[] captions, Runnable[] actions, UIScrollbar bar){
		if (captions.length != actions.length){
			System.err.println("GUI.java:scrollableList(): Invalid list content");
			return;
		}
		int entriesPerPage = (int) Math.floor(size.y / (font.getLineHeight() + SCROLL_LIST_MARGIN));
		if (entriesPerPage < captions.length){
			//Нивлезаит
			if (bar.firstUse){
				bar.maxStates = captions.length - entriesPerPage + 1;
				bar.position = position.cpy().add(size.x * (1 - scrollbarWidth), 0);
				bar.size = size.cpy().scl(scrollbarWidth, 1);
				bar.firstUse = false;
			}
			
			for (int i = 0; i < entriesPerPage; i++){
				button(sr, position.cpy().add(0, size.y * (1 - (i + 1) / (float) entriesPerPage)), 
				       size.cpy().scl(1 - scrollbarWidth, 1 / (float) entriesPerPage), 
				       actions[i + bar.offset], entryColor);
				caption(font, batch, position.cpy().add(0, size.y * (1 - (i + 1) / (float) entriesPerPage)), captions[i + bar.offset]);
			}
			scrollbar(sr, bar, entriesPerPage / (float) captions.length);
		} else {
			//Влезаит
			for (int i = 0; i < captions.length; i++){
				button(sr, position.cpy().add(0, size.y * (1 - (i + 1) / (float) entriesPerPage)), 
				       size.cpy().scl(1, 1 / (float) entriesPerPage), actions[i], entryColor);
				caption(font, batch, position.cpy().add(0, size.y * (1 - (i + 1) / (float) entriesPerPage)), captions[i]);
			}
		}
	}
	
	private void scrollbar(ShapeRenderer sr, UIScrollbar sb, float eppLengthRatio){
		if (!Gdx.input.isTouched())
			sb.isActive = false;
		sr.setColor(sb.color[0]);
		sr.rect(sb.position.x, sb.position.y, sb.size.x, sb.size.y);
		if (UIMouseIsInTheBox(sb.position.x, sb.position.y + sb.size.y * (1 - sb.offset / (float) sb.maxStates), sb.size.x, sb.size.y * eppLengthRatio)){
			sr.setColor(sb.color[2]);
			if (Gdx.input.justTouched())
				sb.isActive = true;
		} else 
			sr.setColor(sb.color[1]);
		
		if (sb.isActive){
			float yy = Utils.UIMousePosition.y;
			yy = Math.min(yy, sb.position.y + sb.size.y);
			yy = Math.max(yy, sb.position.y);
			sb.offset = (int) Math.round((1 - (yy - sb.position.y) / sb.size.y) * sb.maxStates);
		}
		
		sr.rect(sb.position.x, sb.position.y + sb.size.y * (1 - sb.offset / (float) sb.maxStates), sb.size.x, sb.size.y * eppLengthRatio);
	}
	
	public void caption(BitmapFont font, Batch batch, Vector2 position, String text){
		font.draw(batch, text, position.x, position.y);
	}
	
	public boolean UIMouseIsInTheBox(float x, float y, float w, float h){
		return (Utils.UIMousePosition.x > x && Utils.UIMousePosition.x < x + w &&
		        Utils.UIMousePosition.y > y && Utils.UIMousePosition.y < y + h);
	}
}
