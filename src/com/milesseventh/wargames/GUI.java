package com.milesseventh.wargames;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class GUI {
	public class Aligner {
		public Vector2 position, size;
		
		public Aligner(){
			reset();
		}
		
		public void setSize(Vector2 _size){
			normalToUI(size.set(_size), false);
		}
		
		public void next(int hdir, int vdir){
			if (hdir != 0)
				position.x += (size.x + DIM_MARGIN.x) * Math.signum(hdir);
			if (vdir != 0)
				position.y += (size.y + DIM_MARGIN.y) * Math.signum(vdir);
		}
		
		public void reset(){
			position = DIM_DIALOG_REFPOINT.cpy().add(DIM_MARGIN);
			size     = new Vector2(0, 0);
		}
		
	}
	
	class Scrollbar {
		public Vector2 position, 
		                   size, 
		          thumbPosition = new Vector2(), 
		              thumbSize = new Vector2();
		public int offset;
		public boolean initialized = false;
		private float relativeThumbSize, trackingOffset;
		private boolean isVertical, tracking;
		
		public void init(Vector2 _position, Vector2 _size, boolean _isVertical, float _relativeThumbSize){
			position = _position.cpy();
			size = _size.cpy();
			isVertical = _isVertical;
			relativeThumbSize = _relativeThumbSize;
			initialized = true;
		}
		
		public void update(int states){
			--states;//TODO: EffectiveKrutch
			if (offset > states)
				offset = states;
			
			if (isVertical){
				thumbSize.x = size.x;
				thumbSize.y = size.y * relativeThumbSize;
				
				thumbPosition.x = position.x;
				thumbPosition.y = position.y + (size.y - thumbSize.y) * (1f - (float) offset / (float) states);// + thumbSize.y / 2;
			} else {
				thumbSize.x = size.x * relativeThumbSize;
				thumbSize.y = size.y;
				
				thumbPosition.x = position.x + (size.x - thumbSize.x) *       (float) offset / (float) states;// + thumbSize.x / 2;
				thumbPosition.y = position.y;
			}
			
			if (UIMouseHovered(thumbPosition, thumbSize) && Gdx.input.justTouched()){
				if (isVertical)
					trackingOffset = thumbSize.y / 2f + thumbPosition.y - Utils.UIMousePosition.y;
				else
					trackingOffset = thumbSize.x / 2f + thumbPosition.x - Utils.UIMousePosition.x;
				tracking = true;
			}
			if (Utils.isTouchJustReleased)
				tracking = false;
			
			if (tracking)
				if (isVertical){
					float tap = Utils.UIMousePosition.y + trackingOffset;
					tap -= position.y + thumbSize.y / 2;
					tap = MathUtils.clamp(tap, 0, size.y - thumbSize.y);
					tap /= size.y - thumbSize.y;
					offset = Math.round((1 - tap) * states);
				} else {
					float tap = Utils.UIMousePosition.x + trackingOffset;
					tap -= position.x + thumbSize.x / 2;
					tap = MathUtils.clamp(tap, 0, size.x - thumbSize.x);
					tap /= size.x - thumbSize.x;
					offset = Math.round(tap * states);
				}
		}
		
		public void render(Color[] colors){
			sr.setColor(colors[0]);
			sr.rect(position.x, position.y, size.x, size.y);
			sr.setColor(colors[UIMouseHovered(thumbPosition, thumbSize) ? 2 : 1]);
			sr.rect(thumbPosition.x, thumbPosition.y, thumbSize.x, thumbSize.y);
		}
	}
	
	private static final Color[] GUI_COLORS_BUTTON_CLOSE = {
			new Color(.75f, 0, 0, .8f), //default
			new Color(.75f, 0, 0, 1),   //hovered
			new Color(.64f, 0, 0, 1)    //pressed
		};
	public static final Color[] GUI_COLORS_SCROLLBAR_COLORS = {
			new Color(.8f, .8f, .8f, .8f),  //bar background
			new Color(.42f, .42f, .42f, 1), //bar thumb
			new Color(.97f, .97f, .97f, 1)  //hovered
		};
	public static final Color[] GUI_COLORS_DEFAULT = {
			new Color(0, 0, 0, .42f),     //superdefault
			new Color(0, 0, 0, 1),        //hovered
			new Color(.5f, .5f, .5f, .8f) //pressed
		};
	public static Color GUI_COLOR_SEVENT = new Color(218f, 64f, 0f, 1f);
	public static Color GUI_COLOR_TEXT_DEF = new Color(255f, 255f, 255f, 1f);
	public final ObjectiveCallback<String> LAB_RETR_ST_TITLE = new ObjectiveCallback<String>(){
		@Override
		public String call(int id) {
			return Heartstrings.stProperties[id].title;
		}
	};
	
	private static GlyphLayout glay = new GlyphLayout();
	public Structure currentDialogStruct;
	private Scrollbar[] scrollbars = new Scrollbar[32];
	
	public Vector2 DIM_DIALOG_REFPOINT,
	               DIM_DIALOG_SIZE;
	public static Vector2 DIM_MARGIN = new Vector2();
	
	public Vector2 DIM_BUTTON_SIZ_CLOSE,
	               DIM_BUTTON_POS_CLOSE,
	               DIM_BUTTON_CNT_CLOSE;
	
	public static float DIM_VSCROLLBAR_WIDTH = .2f;
	public static final Callback GUI_ACT_BUTTON_CLOSE = new Callback(){
		@Override
		public void action(int source) {
			WG.antistatic.currentDialog = WG.Dialog.NONE;
		}
	};
	
	public GUI(WG _context){
		context = _context;
	}

	private WG context;
	public Batch batch;
	public ShapeRenderer sr;
	public BitmapFont font, subFont;
	public void init(){
		DIM_DIALOG_REFPOINT = new Vector2(0, (WG.UI_H - WG.DIALOG_HEIGHT * WG.UI_H) / 2);
		DIM_DIALOG_SIZE = new Vector2(WG.UI_W, WG.DIALOG_HEIGHT * WG.UI_H);
		DIM_BUTTON_SIZ_CLOSE = new Vector2(WG.UI_W * .1f, WG.UI_H * .05f);
		DIM_BUTTON_POS_CLOSE = DIM_DIALOG_REFPOINT.cpy().add(DIM_DIALOG_SIZE).sub(DIM_BUTTON_SIZ_CLOSE);
		//new Vector2(WG.UI_W, WG.UI_H - (WG.UI_H - WG.UI_H * WG.DIALOG_HEIGHT) / 2).sub(DIM_BUTTON_SIZ_CLOSE);
		
		DIM_MARGIN.x = .01f;
		DIM_MARGIN.y = DIM_MARGIN.x * DIM_DIALOG_SIZE.x / DIM_DIALOG_SIZE.y;
		DIM_MARGIN.scl(DIM_DIALOG_SIZE);
		aligner = new Aligner();
		
		//DIM_BUTTON_CNT_CLOSE = DIM_BUTTON_POS_CLOSE.cpy().sub(DIM_BUTTON_SIZ_CLOSE.cpy().scl(.5f));
		for (int i = 0; i < scrollbars.length; ++i)
			if (scrollbars[i] != null)
				scrollbars[i].initialized = false;
			else
				scrollbars[i] = new Scrollbar();
	}
	
	Aligner aligner;
	public void dialog(WG.Dialog dialog){
		//Drawing dialog backround and close button
		sr.setColor(GUI_COLORS_DEFAULT[0]);
		sr.rect(DIM_DIALOG_REFPOINT.x, DIM_DIALOG_REFPOINT.y, DIM_DIALOG_SIZE.x, DIM_DIALOG_SIZE.y);
		button(DIM_BUTTON_POS_CLOSE, DIM_BUTTON_SIZ_CLOSE, -1, GUI_ACT_BUTTON_CLOSE, GUI_COLORS_BUTTON_CLOSE);
		
		//Drawing layout
		switch (dialog){
		case CRAFTING:
			break;
		case LABORATORY:
			/*if (!scrollbars[0].initialized){
				aligner.setSize(Utils.getVector(.7f, .12f));
				scrollbars[0].init(aligner.position, aligner.size, false, .5f);
			}
			scrollbars[0].update(3);
			scrollbars[0].render(GUI_COLORS_SCROLLBAR_COLORS);*/
			aligner.setSize(Utils.getVector(.4f, 1f));
			list(aligner.position, aligner.size, Heartstrings.stProperties.length, DEBUG_LEC, GUI_COLORS_DEFAULT, 0);
			break;
		case NONE:
			break;
		default:
			break;
		}
		
		aligner.reset();
	}
	
	ListEntryCallback DEBUG_LEC = new ListEntryCallback() {
		@Override
		public void action(int id) {
			System.out.println(id);
		}
		
		@Override
		public void entry(Vector2 position, Vector2 size, int id, Color[] color) {
			advancedButton(position, size, id, this, color, Heartstrings.stProperties[id].title, Heartstrings.stProperties[id].description, null);
		}

	};
	
	private static final float SCROLLBAR_RWIDTH = .1f, LIST_ENTRY_HEIGHT = 43f;
	public void list(Vector2 position, Vector2 size, int entries, ListEntryCallback entry, Color[] colors, int scrollID){
		size.y = Math.round(size.y / LIST_ENTRY_HEIGHT) * LIST_ENTRY_HEIGHT;
		int entriesPerPage = Math.round(size.y / LIST_ENTRY_HEIGHT);
		int offset = 0;
		float listW = size.x, barW;
		
		if (entriesPerPage < entries){
			barW = listW * SCROLLBAR_RWIDTH;
			listW -= barW;
			if (!scrollbars[scrollID].initialized){
				scrollbars[scrollID].init(Utils.getVector(position).add(listW, 0), 
				                          Utils.getVector(barW, size.y), 
				                          true, (float) entriesPerPage / (float) entries);
			}
			int states = entries - entriesPerPage + 1;
			scrollbars[scrollID].update(states);
			scrollbars[scrollID].render(GUI_COLORS_SCROLLBAR_COLORS);
			offset = scrollbars[scrollID].offset;
		}
		
		for (int i = offset; i < Math.min(entriesPerPage, entries) + offset; ++i){
			entry.entry(Utils.getVector(position).add(0, size.y - (i - offset + 1) * LIST_ENTRY_HEIGHT), 
			            Utils.getVector(listW, LIST_ENTRY_HEIGHT), i, colors);
		}
	}
	
	private static final int PIE_MENU_SECTOR_MARGIN = 5;
	public void piemenu(Vector2 position, float radius, Color unselected, Color selected, int size, Callback action){
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
	
	private static final int PROMPT_BORDER = 10;
	private void prompt(Color back, String prompt){
		String text = Utils.splitIntoLines(prompt, 32);
		glay.setText(subFont, text);
		sr.setColor(back);
		Vector2 corner = Utils.getVector(Utils.UIMousePosition.x + 5, Math.max(Utils.UIMousePosition.y - glay.height - 5, 0));
		sr.rect(corner.x, corner.y, glay.width + PROMPT_BORDER * 2, glay.height + PROMPT_BORDER * 2);
		caption(corner.add(PROMPT_BORDER, PROMPT_BORDER), text, subFont, true, null);
	}
	
	public void button(Vector2 position, Vector2 size, int id, Callback callback, Color[] colors){
		Color color = colors[0];
		if (UIMouseHovered(position.x, position.y, size.x, size.y)){
			color = colors[1];
			if (Gdx.input.isTouched())
				color = colors[2];
			if (Utils.isTouchJustReleased)
				callback.action(id);
		}
		sr.setColor(color);
		sr.rect(position.x, position.y, size.x, size.y);
	}

	public void advancedButton(Vector2 position, Vector2 size, int id, Callback callback, Color[] colors, 
		                       String caption, String prompt, Color textColor){
		button(position, size, id, callback, colors);
		if (caption != null){
			glay.setText(font, caption);
			caption(Utils.getVector(position).add(DIM_MARGIN.x, (size.y - glay.height) * .5f), caption, font, true, textColor);
		}
		if (prompt != null && UIMouseHovered(position, size)){
			prompt(GUI_COLORS_DEFAULT[0], prompt);
		}
	}
	
	public void caption(Vector2 position, String text, BitmapFont font, boolean alignToBottom, Color color){
		sr.flush();
		glay.setText(font, text);
		
		batch.begin();
		if (color == null)
			font.setColor(GUI_COLOR_TEXT_DEF);
		else
			font.setColor(color);
		font.draw(batch, text, position.x, position.y + (alignToBottom ? glay.height : 0));
		batch.end();

		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
	}

	public boolean UIMouseHovered(Vector2 position, Vector2 size){
		return UIMouseHovered(position.x, position.y, size.x, size.y);
	}
	
	public boolean UIMouseHovered(float x, float y, float w, float h){
		return (Utils.UIMousePosition.x > x && Utils.UIMousePosition.x < x + w &&
		        Utils.UIMousePosition.y > y && Utils.UIMousePosition.y < y + h);
	}
	
	//Modifies given vector, returns chaining variable
	public Vector2 normalToUI(Vector2 in, boolean isCoordinate){
		return in.scl(WG.UI_W, DIM_DIALOG_SIZE.y - DIM_BUTTON_SIZ_CLOSE.y - DIM_MARGIN.y * 2).add(isCoordinate ? DIM_DIALOG_REFPOINT : Vector2.Zero);
	}
}
