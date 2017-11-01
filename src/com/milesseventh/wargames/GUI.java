package com.milesseventh.wargames;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
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
	//Occupied: 0-7 ex: 1
	private UIScrollbar[] sb = {new UIScrollbar(), new UIScrollbar(), new UIScrollbar(), 
	                            new UIScrollbar(), new UIScrollbar(), new UIScrollbar(), 
	                            new UIScrollbar(), new UIScrollbar(), new UIScrollbar(), 
	                            new UIScrollbar(), new UIScrollbar(), new UIScrollbar(), 
	                            new UIScrollbar(), new UIScrollbar(), new UIScrollbar(), 
	                            new UIScrollbar(), new UIScrollbar(), new UIScrollbar()};//bleh
	
	public Vector2 DIM_DIALOG_REFPOINT,// = new Vector2(0, (WG.UI_H - WG.DIALOG_HEIGHT * WG.UI_H) / 2),
	               DIM_DIALOG_SIZE;// = new Vector2(WG.UI_W, WG.DIALOG_HEIGHT * WG.UI_H);
	
	public Vector2 DIM_BUTTON_SIZ_CLOSE,// = new Vector2(WG.UI_W * .1f, WG.UI_H * .05f),//Close dialog button
	               DIM_BUTTON_POS_CLOSE,// = new Vector2(WG.UI_W, WG.UI_H - (WG.UI_H - WG.UI_H * WG.DIALOG_HEIGHT) / 2).sub(DIM_BUTTON_SIZ_CLOSE),
	               DIM_BUTTON_CNT_CLOSE;// = DIM_BUTTON_POS_CLOSE.cpy().sub(DIM_BUTTON_SIZ_CLOSE.cpy().scl(.5f));
	
	public static final float DIM_MARGIN = .01f,  DIM_VSCROLLBAR_WIDTH = .2f; 
	public static final Croupfuck GUI_BUTTON_ACT_CLOSE = new Croupfuck(){
		@Override
			public void action(int source) {
			WG.antistatic.currentDialog = WG.Dialog.NONE;
		}
	};
	
	public GUI(WG _context){
		context = _context;
	}
	
	public void init(){
		DIM_DIALOG_REFPOINT = new Vector2(0, (WG.UI_H - WG.DIALOG_HEIGHT * WG.UI_H) / 2);
		DIM_DIALOG_SIZE = new Vector2(WG.UI_W, WG.DIALOG_HEIGHT * WG.UI_H);
		DIM_BUTTON_SIZ_CLOSE = new Vector2(WG.UI_W * .1f, WG.UI_H * .05f);
		DIM_BUTTON_POS_CLOSE = new Vector2(WG.UI_W, WG.UI_H - (WG.UI_H - WG.UI_H * WG.DIALOG_HEIGHT) / 2).sub(DIM_BUTTON_SIZ_CLOSE);
		DIM_BUTTON_CNT_CLOSE = DIM_BUTTON_POS_CLOSE.cpy().sub(DIM_BUTTON_SIZ_CLOSE.cpy().scl(.5f));
		for (UIScrollbar bar: sb)
			bar.firstUse = true;
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
	
	public void buttonWithCaption(Vector2 position, Vector2 size, int id, Croupfuck callback, Color[] colors, String caption){
		button(position, size, id, callback, colors);
		caption(Utils.getVector(position).add(0, size.y * .9f), caption);
	}
	
	private static final int PIE_MENU_SECTOR_MARGIN = 5;
	public void piemenu(Vector2 position, float radius, Color unselected, Color selected, int size, Croupfuck action){
		for(int i = 0; i < size; i++){
			float angle = Utils.getAngle(context.getUIFromWorldV(Utils.WorldMousePosition).sub(position));
			if (angle > i * (360 / (float) size) + PIE_MENU_SECTOR_MARGIN &&
			    angle < (i + 1) * (360 / (float) size) + PIE_MENU_SECTOR_MARGIN){
				if (Utils.isTouchJustReleased)
					action.action(i);
				sr.setColor(selected);
			} else
				sr.setColor(unselected);
			Utils.drawTrueArc(sr, position, 20, i * (360 / (float) size) + PIE_MENU_SECTOR_MARGIN, (360 / (float) size) - 2 * PIE_MENU_SECTOR_MARGIN, 70);
		}
	}
	
	public void dialog(WG.Dialog dialog){
		sr.setColor(WG.GUI_DIALOG_BGD);
		sr.rect(DIM_DIALOG_REFPOINT.x, DIM_DIALOG_REFPOINT.y, DIM_DIALOG_SIZE.x, DIM_DIALOG_SIZE.y);
		button(DIM_BUTTON_POS_CLOSE, DIM_BUTTON_SIZ_CLOSE, Utils.NULL_ID, GUI_BUTTON_ACT_CLOSE, GUI_BUTTON_CLOSE_COLORS);
		
		switch (dialog){
		case LABORATORY:
			scrollableList(normalToUI(Utils.getVector(DIM_MARGIN, DIM_MARGIN), true),
			               normalToUI(Utils.getVector(.3625f, 1), false),
			               DIM_VSCROLLBAR_WIDTH / 2f, ScrollEntry.LAB_SPECIALS, WG.GUI_BUTTON_DEFAULT_COLORS,
			               Fraction.specialTechnologyTitles, DBG_LIST_ACT, sb[0]);
			for (int i = 0; i < Fraction.Technology.values().length; i++){
				hscroller(normalToUI(Utils.getVector(.5f, .95f - i * .06f), true),
				          normalToUI(Utils.getVector(.2f, .05f), false),
				          sb[2 + i], .42f, (int)Fraction.MAXPRIOR);
				caption(normalToUI(Utils.getVector(.5f + .25f, 1 - i * .06f), true),
				        Fraction.technologyTitles[i] + ": " + String.format("%.2f", WG.antistatic.sm.getCurrent().techPriorities[i] / (float)Fraction.MAXPRIOR * 100  + '%'));
				WG.antistatic.sm.getCurrent().techPriorities[i] = sb[2 + i].offset;
			}
			break;
		default:
			break;
		}
	}
	
	private static final int SCROLL_LIST_MARGIN = 2;
	public void scrollableList(Vector2 position, Vector2 size, float scrollbarWidth, ScrollEntry type,
	                           Color[] entryColor, String[] captions, Croupfuck actions, UIScrollbar bar){
		sr.setColor(Utils.getColor(0, 0, 0, 17));
		sr.rect(position.x, position.y, size.x, size.y);
		
		int entriesPerPage = (int) Math.floor(size.y / (font.getLineHeight() + SCROLL_LIST_MARGIN));
		if (entriesPerPage < captions.length){
			//Нивлезаит
			if (bar.firstUse){
				bar.max = captions.length - entriesPerPage + 1;
				bar.position = position.cpy().add(size.x * (1 - scrollbarWidth), 0);
				bar.size = size.cpy().scl(scrollbarWidth, 1);
				bar.firstUse = false;
			}
			
			bar.offset = MathUtils.clamp(bar.offset, 0, bar.max - 1);
			
			for (int i = 0; i < entriesPerPage; i++){
				scrollEntry(type, Utils.getVector(position).add(0, size.y * (1 - (i + 1) / (float) entriesPerPage)), 
				            Utils.getVector(size).scl(1 - scrollbarWidth, 1 / (float) entriesPerPage), i + bar.offset, actions, entryColor, captions[i + bar.offset]);
			}
			scrollbar(bar, entriesPerPage / (float) captions.length);
		} else {
			//Влезаит
			for (int i = 0; i < captions.length; i++){
				scrollEntry(type, Utils.getVector(position).add(0, size.y * (1 - (i + 1) / (float) entriesPerPage)), 
				            Utils.getVector(size).scl(1, 1 / (float) entriesPerPage), i + bar.offset, actions, entryColor, captions[i]);
			}
		}
	}
	
	private enum ScrollEntry{ORDINARY, LAB_SPECIALS}
	private void scrollEntry(ScrollEntry type, Vector2 position, Vector2 size, int id, Croupfuck action, Color[] entryColor, String caption){
		buttonWithCaption(position, size, id, action, entryColor, caption);
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
	
	private void hscroller(Vector2 position, Vector2 size, UIScrollbar sb, float widthPart, int max){
		if (sb.firstUse){
			sb.max = max;
			sb.position = position.cpy();
			sb.size = size.cpy();
			sb.firstUse = false;
		}
		
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
	
	public Vector2 normalToUI(Vector2 in, boolean isCoordinate){
		return in.scl(WG.UI_W, DIM_DIALOG_SIZE.y - DIM_BUTTON_SIZ_CLOSE.y - GUI.DIM_MARGIN * 2 * WG.UI_H).add(isCoordinate?DIM_DIALOG_REFPOINT:Vector2.Zero);
	}
}
