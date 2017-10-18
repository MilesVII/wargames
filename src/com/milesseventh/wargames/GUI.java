package com.milesseventh.wargames;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
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
			new Color(.42f, .42f, .42f, 1), 
			new Color(.97f, .97f, .97f, 1)
		};
	
	public class UIScrollbar{
		public boolean isActive = false, firstUse = true;
		public int maxStates = -1, offset = 0;
		public Vector2 position, size;
		public Color[] color = GUI_SCROLLBAR_COLORS;//0 -- Bar bgd, 1 -- Bar normal, 2 -- Bar hovered/pressed
	}
	
	private WG context;
	public Batch batch;
	public ShapeRenderer sr;
	public BitmapFont font;
	private UIScrollbar dbg_sb1 = new UIScrollbar(), dbg_sb2 = new UIScrollbar();
	public GUI(WG _context){
		context = _context;
	}

	public void button(Vector2 position, Vector2 size, Runnable callback, Color[] colors){
		Color color = colors[0];
		if (UIMouseHovered(position.x, position.y, size.x, size.y)){
			color = colors[1];
			if (Gdx.input.isTouched())
				color = colors[2];
			if (Gdx.input.justTouched())
				callback.run();
		}
		sr.setColor(color);
		sr.rect(position.x, position.y, size.x, size.y);
	}
	
	public void buttonWithCaption(Vector2 position, Vector2 size, Runnable callback, Color[] colors, String caption){
		button(position, size, callback, colors);
		caption(Utils.getVector(position).add(0, size.y * .9f), caption);
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

	private static final Vector2 DBG_DIM_SL_POS = new Vector2(0, (WG.UI_H - WG.DIALOG_HEIGHT * WG.UI_H) / 2).add(10, 10);
	private static final Vector2 DBG_DIM_SL_SIZ = new Vector2(WG.UI_W * .3f, WG.DIALOG_HEIGHT * WG.UI_H / 2 - 20);
	private static final float DBG_DIM_SB_W = .12f;
	public void dialog(WG.Dialog dialog){
		sr.setColor(WG.GUI_DIALOG_BGD);
		sr.rect(0, (WG.UI_H - WG.DIALOG_HEIGHT * WG.UI_H) / 2, WG.UI_W, WG.DIALOG_HEIGHT * WG.UI_H);
		button(WG.GUI_BUTTON_POS_CLOSE, WG.GUI_BUTTON_SIZ_CLOSE, WG.GUI_BUTTON_ACT_CLOSE, GUI_BUTTON_CLOSE_COLORS);
		
		switch (dialog){
		case UNITS_BUILDING:
			String[] str = {"1", "2", "3", "4", "5", "1", "2", "3", "4", "5", "1", "2", "3", "4", "5"};
			Runnable[] run = {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null};
			scrollableList(DBG_DIM_SL_POS, DBG_DIM_SL_SIZ, DBG_DIM_SB_W, WG.GUI_BUTTON_DEFAULT_COLORS,
			               str, run, dbg_sb1);
			//scrollableList(DBG_DIM_SL_POS.cpy().add(DBG_DIM_SL_SIZ.x * 1.7f, 0), DBG_DIM_SL_SIZ, DBG_DIM_SB_W, WG.GUI_BUTTON_DEFAULT_COLORS,
			//           str, run, dbg_sb2);
			buttonWithCaption(Utils.getVector(DBG_DIM_SL_POS.x + DBG_DIM_SL_SIZ.x * 1.7f, DBG_DIM_SL_POS.y), Utils.getVector(200, 20), null, GUI_BUTTON_CLOSE_COLORS, "CAPTION");
		//caption(font, batch, new Vector2(200, 200), "KFNIE");
			break;
		case RESOURCE_MANAGER:
			break;
		case UNITS_ASSEMBLY:
			break;
		default:
			break;
		}
	}
	
	private static final int SCROLL_LIST_MARGIN = 2;
	public void scrollableList(Vector2 position, Vector2 size, float scrollbarWidth, 
	                           Color[] entryColor, String[] captions, Runnable[] actions, UIScrollbar bar){
		if (captions.length != actions.length){
			System.err.println("GUI.java:scrollableList(): Invalid list content");
			return;
		}
		//sr.setColor(Color.CYAN);
		//sr.rect(position.x, position.y, size.x, size.y);
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
				button(Utils.getVector(position).add(0, size.y * (1 - (i + 1) / (float) entriesPerPage)), 
				       Utils.getVector(size).scl(1 - scrollbarWidth, 1 / (float) entriesPerPage), 
				       actions[i + bar.offset], entryColor);
				caption(Utils.getVector(position).add(2, size.y * (1 - i / (float) entriesPerPage) - 1), captions[i + bar.offset]);
			}
			scrollbar(bar, entriesPerPage / (float) captions.length);
		} else {
			//Влезаит
			for (int i = 0; i < captions.length; i++){
				button(Utils.getVector(position).add(0, size.y * (1 - (i + 1) / (float) entriesPerPage)), 
						Utils.getVector(size).scl(1, 1 / (float) entriesPerPage), actions[i], entryColor);
				caption(Utils.getVector(position).add(2, size.y * (1 - i / (float) entriesPerPage) - 1), captions[i]);
			}
		}
	}
	
	private void scrollbar(UIScrollbar sb, float eppLengthRatio){
		if (!Gdx.input.isTouched())
			sb.isActive = false;
		sr.setColor(sb.color[0]);
		sr.rect(sb.position.x, sb.position.y, sb.size.x, sb.size.y);
		float sb_h = sb.size.y * eppLengthRatio;
		if (UIMouseHovered(sb.position.x, sb.position.y + (1 - sb.offset / (float) (sb.maxStates)) * (sb.size.y - sb_h), sb.size.x, sb_h)){
			sr.setColor(sb.color[2]);
			if (Gdx.input.justTouched())
				sb.isActive = true;
		} else 
			sr.setColor(sb.color[1]);
		
		if (sb.isActive){
			sr.setColor(sb.color[2]);
			float yy = Utils.UIMousePosition.y;
			yy = Math.min(yy, sb.position.y + sb.size.y - sb_h / 2f);
			yy = Math.max(yy, sb.position.y + sb_h / 2f);
			yy = 1 - (yy - sb.position.y - sb_h / 2f) / (float) (sb.size.y - sb_h);
			sb.offset = (int) Math.round(yy * (sb.maxStates - 1));
		}
		
		sr.rect(sb.position.x, sb.position.y + (1 - sb.offset / (float) (sb.maxStates - 1)) * (sb.size.y - sb_h), sb.size.x, sb_h);
	}
	
	public void caption(Vector2 position, String text){
		sr.flush();
		
		batch.begin();
		font.draw(batch, text, position.x, position.y);
		batch.end();

		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
	}

	public boolean UIMouseHovered(float x, float y, float w, float h){
		return (Utils.UIMousePosition.x > x && Utils.UIMousePosition.x < x + w &&
		        Utils.UIMousePosition.y > y && Utils.UIMousePosition.y < y + h);
	}
}
