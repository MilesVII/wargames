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
	public static final Croupfuck DBG_LIST_ACT = new Croupfuck(){
		@Override
		public void action(int source) {
			System.out.println("" + source + " pressed");
		}
	};
	public class UIScrollbar{
		public boolean isActive = false, firstUse = true;
		public int max = -1, offset = 0;
		public Vector2 position, size;
		public Color[] color = GUI_SCROLLBAR_COLORS;//0 -- Bar bgd, 1 -- Bar normal, 2 -- Bar hovered/pressed
	}
	
	private WG context;
	public Batch batch;
	public ShapeRenderer sr;
	public BitmapFont font;
	public Structure currentDialogStruct;
	private UIScrollbar dbg_sb1 = new UIScrollbar(), dbg_sb2 = new UIScrollbar(), dbg_sb3 = new UIScrollbar();
	public GUI(WG _context){
		context = _context;
	}
	
	public void button(Vector2 position, Vector2 size, int id, Croupfuck callback, Color[] colors){
		Color color = colors[0];
		if (UIMouseHovered(position.x, position.y, size.x, size.y)){
			color = colors[1];
			if (Gdx.input.isTouched())
				color = colors[2];
			if (Gdx.input.justTouched())
				callback.action(id);
		}
		sr.setColor(color);
		sr.rect(position.x, position.y, size.x, size.y);
	}
	
	public void buttonWithCaption(Vector2 position, Vector2 size, Croupfuck callback, Color[] colors, String caption){
		button(position, size, Utils.NULL_ID, callback, colors);
		caption(Utils.getVector(position).add(0, size.y * .9f), caption);
	}
	
	private static final int PIE_MENU_SECTOR_MARGIN = 5;
	public void piemenu(Vector2 position, float radius, Color unselected, Color selected, int size, Croupfuck action){
		for(int i = 0; i < size; i++){
			float angle = Utils.getAngle(context.getUIFromWorldV(Utils.WorldMousePosition).sub(position));
			if (angle > i * (360 / (float) size) + PIE_MENU_SECTOR_MARGIN &&
			    angle < (i + 1) * (360 / (float) size) + PIE_MENU_SECTOR_MARGIN){
				if (Utils.isTouchJustReleased)
					action.action(i);;
				sr.setColor(selected);
			} else
				sr.setColor(unselected);
			Utils.drawTrueArc(sr, position, 20, i * (360 / (float) size) + PIE_MENU_SECTOR_MARGIN, (360 / (float) size) - 2 * PIE_MENU_SECTOR_MARGIN, 70);
		}
	}

	private static final Vector2 DBG_DIM_SL_POS = new Vector2(0, (WG.UI_H - WG.DIALOG_HEIGHT * WG.UI_H) / 2).add(10, 10);
	private static final Vector2 DBG_DIM_SL_SIZ = new Vector2(WG.UI_W * .3f, WG.DIALOG_HEIGHT * WG.UI_H / 2 - 20);
	private static final float DBG_DIM_SB_W = .12f;
	public void dialog(WG.Dialog dialog){
		sr.setColor(WG.GUI_DIALOG_BGD);
		sr.rect(0, (WG.UI_H - WG.DIALOG_HEIGHT * WG.UI_H) / 2, WG.UI_W, WG.DIALOG_HEIGHT * WG.UI_H);
		button(WG.GUI_BUTTON_POS_CLOSE, WG.GUI_BUTTON_SIZ_CLOSE, Utils.NULL_ID, WG.GUI_BUTTON_ACT_CLOSE, GUI_BUTTON_CLOSE_COLORS);
		
		switch (dialog){
		case LABORATORY:
			String[] str = {"1", "2", "3", "4", "5", "1", "2", "3", "4", "5", "1", "2", "3", "4", "5"};
			
			//scrollableList(DBG_DIM_SL_POS, DBG_DIM_SL_SIZ, DBG_DIM_SB_W, WG.GUI_BUTTON_DEFAULT_COLORS,
			//               str, DBG_LIST_ACT, dbg_sb1);
			//scrollableList(DBG_DIM_SL_POS.cpy().add(DBG_DIM_SL_SIZ.x * 1.7f, 0), DBG_DIM_SL_SIZ, DBG_DIM_SB_W, WG.GUI_BUTTON_DEFAULT_COLORS,
			//           str, run, dbg_sb2);
			hscroller(dbg_sb3, .1f, -1);
			buttonWithCaption(Utils.getVector(DBG_DIM_SL_POS.x + DBG_DIM_SL_SIZ.x * 1.7f, DBG_DIM_SL_POS.y), Utils.getVector(200, 20), null, GUI_BUTTON_CLOSE_COLORS, "" + currentDialogStruct.getResource(Structure.Resource.ORE));
		//caption(font, batch, new Vector2(200, 200), "KFNIE");
			break;
		default:
			break;
		}
	}
	
	private static final int SCROLL_LIST_MARGIN = 2;
	public void scrollableList(Vector2 position, Vector2 size, float scrollbarWidth, 
	                           Color[] entryColor, String[] captions, Croupfuck actions, UIScrollbar bar){
		/*if (captions.length != actions.length){
			System.err.println("GUI.java:scrollableList(): Invalid list content");
			return;
		}*/
		int entriesPerPage = (int) Math.floor(size.y / (font.getLineHeight() + SCROLL_LIST_MARGIN));
		if (entriesPerPage < captions.length){
			//Нивлезаит
			if (bar.firstUse){
				bar.max = captions.length - entriesPerPage + 1;
				bar.position = position.cpy().add(size.x * (1 - scrollbarWidth), 0);
				bar.size = size.cpy().scl(scrollbarWidth, 1);
				bar.firstUse = false;
			}
			
			for (int i = 0; i < entriesPerPage; i++){
				button(Utils.getVector(position).add(0, size.y * (1 - (i + 1) / (float) entriesPerPage)), 
				       Utils.getVector(size).scl(1 - scrollbarWidth, 1 / (float) entriesPerPage),
				       i + bar.offset, actions, entryColor);
				caption(Utils.getVector(position).add(2, size.y * (1 - i / (float) entriesPerPage) - 1), captions[i + bar.offset]);
			}
			scrollbar(bar, entriesPerPage / (float) captions.length);
		} else {
			//Влезаит
			for (int i = 0; i < captions.length; i++){
				button(Utils.getVector(position).add(0, size.y * (1 - (i + 1) / (float) entriesPerPage)), 
						Utils.getVector(size).scl(1, 1 / (float) entriesPerPage), i + bar.offset, actions, entryColor);
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
		if (UIMouseHovered(sb.position.x, sb.position.y + (1 - sb.offset / (float) (sb.max)) * (sb.size.y - sb_h), sb.size.x, sb_h)){
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
			sb.offset = (int) Math.round(yy * (sb.max - 1));
		}
		
		sr.rect(sb.position.x, sb.position.y + (1 - sb.offset / (float) (sb.max - 1)) * (sb.size.y - sb_h), sb.size.x, sb_h);
	}	
	
	private void hscroller(UIScrollbar sb, float widthPart, int limit){
		if (sb.firstUse){
			sb.max = 7;
			sb.position = new Vector2(20, DBG_DIM_SL_POS.y + 40);
			sb.size = new Vector2(270, 20);
			sb.firstUse = false;
		}
		
		caption(Utils.getVector(sb.position).add(sb.size.x, 0), "Off: " + sb.offset + "/" + sb.max);
		
		if (!Gdx.input.isTouched())
			sb.isActive = false;
		
		sr.setColor(sb.color[0]);
		sr.rect(sb.position.x, sb.position.y, sb.size.x, sb.size.y);
		
		float sb_w = sb.size.x * widthPart, 
		      sb_offx = (sb.offset / (float) (sb.max)) * (sb.size.x - sb_w);
		
		if (UIMouseHovered(sb.position.x + sb_offx, sb.position.y, sb_w, sb.size.y)){
			sr.setColor(sb.color[2]);
			if (Gdx.input.justTouched())
				sb.isActive = true;
		} else 
			sr.setColor(sb.color[1]);
		
		if (sb.isActive){
			sr.setColor(sb.color[2]);
			float xx = Utils.UIMousePosition.x - sb_w / 2;
			xx -= sb.position.x;
			xx = Math.min(xx, sb.size.x - sb_w);
			xx = Math.max(xx, 0);
			xx = (xx/*sb_w / 2*/) / (sb.size.x - sb_w);
			sb.offset = (int) Math.round(xx * (sb.max));
		}
		
		sr.rect(sb.position.x + sb_offx, sb.position.y, sb_w, sb.size.y);
	}
	
	public void caption(Vector2 position, String text){
		sr.flush();
		
		batch.begin();
		font.draw(batch, text, position.x, position.y);
		batch.end();

		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
	}
	
	public <T> T radio(T[] list, String caps, Vector2 position, Vector2 size){
		return null;
	}

	public boolean UIMouseHovered(float x, float y, float w, float h){
		return (Utils.UIMousePosition.x > x && Utils.UIMousePosition.x < x + w &&
		        Utils.UIMousePosition.y > y && Utils.UIMousePosition.y < y + h);
	}
}
