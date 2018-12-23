package com.milesseventh.wargames;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.milesseventh.wargames.Heartstrings.Craftable;
import com.milesseventh.wargames.Heartstrings.SpecialTechnology;
import com.milesseventh.wargames.Heartstrings.Technology;
import com.milesseventh.wargames.dialogs.CraftingDialog;
import com.milesseventh.wargames.dialogs.TradeDialog;
import com.milesseventh.wargames.dialogs.YardDialog;
import com.milesseventh.wargames.properties.SpecialTechnologyProperties;

public class GUI {
	public class Aligner {
		public Vector2 position, size;
		private Vector2 refPosition;
		
		public Aligner(){
			position = new Vector2();
			reset();
		}
		
		public void setSize(Vector2 _size){
			setSize(_size.x, _size.y);
		}
		
		public void setSize(float x, float y){
			normalToUI(size.set(x, y), false);
			size.sub(DIM_MARGIN).sub(DIM_MARGIN);
		}
		
		public void next(int hdir, int vdir){
			if (hdir != 0)
				refPosition.x += (size.x + DIM_MARGIN.x * 2f) * hdir;
			if (vdir != 0)
				refPosition.y += (size.y + DIM_MARGIN.y * 2f) * vdir;
			restorePosition();
		}
		
		public void shift(float x, float y, int hdir, int vdir){
			setSize(x, y);
			next(hdir, vdir);
		}
		
		public void reset(){
			refPosition = DIM_DIALOG_REFPOINT.cpy();
			restorePosition();
			size = new Vector2(0, 0);
		}
		
		private void restorePosition(){
			position.set(refPosition).add(DIM_MARGIN);
		}
	}
	
	class Scrollbar {
		public Vector2 position, 
		                   size, 
		          thumbPosition = new Vector2(), 
		              thumbSize = new Vector2();
		public static final int GUI_SB_DEFAULT_STATES = 101;
		public static final int GUI_SB_DEFAULT_MAXVAL = GUI_SB_DEFAULT_STATES - 1;
		public static final float GUI_SB_DEFAULT_THUMB = .42f;
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
		
		/**
		 * update method is getting amount of possible states as parameter
		 * states = maxvalue + 1
		 */
		public void update(int states){
			if (states > 0)
				--states;
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
			new Color(0, 0, 0, .64f),     //superdefault
			new Color(0, 0, 0, 1),        //hovered
			new Color(.5f, .5f, .5f, .8f) //pressed
		};
	public static final Color[] GUI_COLORS_TRANSPARENT = {
			new Color(0, 0, 0, 0), //superdefault
			new Color(0, 0, 0, 0), //hovered
			new Color(0, 0, 0, 0)  //pressed
		};
	public static final Color[] GUI_COLORS_SEVENTH_PROGRESS = {
			new Color(218f/255f, 64f/255f, 0, 1f),     //done
			new Color(0, 0, 0, 0),                     //left
		};
	public static final Color[] GUI_COLORS_GREENRED_PROGRESS = {
			new Color(0, 1f, 0, 1f),  //done
			new Color(1f, 0, 0, 1f),  //left
		};
	public static final Color[] GUI_COLORS_SEVENTH_PROGRESS_TRANSPARENT = {
			new Color(218f/255f, 64f/255f, 0, .84f), //done
			new Color(0, 0, 0, 0),                   //left
		};
	public static final Color[] GUI_COLORS_PROGRESS_REPAIRING_TRANSPARENT = {
			new Color(0, 1f, 0, 1f),  //done
			new Color(0,  0, 0,  0),  //left
		};
	public static final Color[] GUI_COLORS_PROGRESS_DAMAGED_TRANSPARENT = {
			new Color(1f, 0, 0, 1f), //done
			new Color( 0, 0, 0,  0), //left
		};
	
	public static final Color GUI_COLOR_SEVENTH = new Color(218/255f, 64/255f, 0f, 1f);
	public static final Color GUI_COLOR_TEXT_DEF = new Color(1f, 1f, 1f, 1f);
	public final ObjectiveCallback<String> LAB_RETR_ST_TITLE = new ObjectiveCallback<String>(){
		@Override
		public String call(int id) {
			return Heartstrings.stProperties[id].title;
		}
	};
	
	private static GlyphLayout glay = new GlyphLayout();
	//Engaged 0; 1-6; 11; 12; 13-18; 22; 23; 24; 25-30; 31, 32, 33, 34, 35
	//Reserved 7-10; 19-21;
	private Scrollbar[] scrollbars = new Scrollbar[64];
	
	public Vector2 DIM_DIALOG_REFPOINT,
	               DIM_DIALOG_SIZE;
	public static Vector2 DIM_MARGIN = new Vector2();
	
	public Vector2 DIM_BUTTON_SIZ_CLOSE,
	               DIM_BUTTON_POS_CLOSE,
	               DIM_BUTTON_CNT_CLOSE;
	
	public static float DIM_VSCROLLBAR_WIDTH = .2f;
	public final Callback GUI_ACT_BUTTON_CLOSE = new Callback(){
		@Override
		public void action(int source) {
			yardDialogState.selectedUnitsForDeployment.clear();
			WG.antistatic.uistate = WG.UIState.FREE;
		}
	};
	
	public GUI(WG _context){
		context = _context;
	}

	private WG context;
	public Batch batch;
	public ShapeRenderer sr;
	public BitmapFont font, subFont;
	public void init(float marginReference){
		DIM_DIALOG_REFPOINT = new Vector2(0, (WG.UI_H - WG.DIALOG_HEIGHT * WG.UI_H) / 2);
		DIM_DIALOG_SIZE = new Vector2(WG.UI_W, WG.DIALOG_HEIGHT * WG.UI_H);
		DIM_BUTTON_SIZ_CLOSE = new Vector2(WG.UI_W * .1f, WG.UI_H * .05f);
		DIM_BUTTON_POS_CLOSE = DIM_DIALOG_REFPOINT.cpy().add(DIM_DIALOG_SIZE).sub(DIM_BUTTON_SIZ_CLOSE);
		//new Vector2(WG.UI_W, WG.UI_H - (WG.UI_H - WG.UI_H * WG.DIALOG_HEIGHT) / 2).sub(DIM_BUTTON_SIZ_CLOSE);
		
		DIM_MARGIN.x = marginReference;
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
	
	//LAB: Chooses which ST to investigate
	private final ListEntryCallback GUI_LEC_ST = new ListEntryCallback() {
		@Override
		public void action(int id) {
			if (Faction.debug.isSTInvestigationPossibleRightNow(id))
				Faction.debug.startInvestigatingSpecialTechnology(id);
		}
		
		@Override
		public void entry(Vector2 position, Vector2 size, int id, Color[] color) {
			if (Heartstrings.stProperties[id].areBasicSTInvestigated(Faction.debug)){
				SpecialTechnologyProperties st = Heartstrings.stProperties[id];
				Color c = null;
				if (Faction.debug.isInvestigated(Heartstrings.SpecialTechnology.values()[id]))
					c = Color.GREEN;
				if (Faction.debug.isBeingInvestigated(Heartstrings.SpecialTechnology.values()[id]))
					c = GUI_COLOR_SEVENTH;
				advancedButton(position, size, id, this, color, 
				               st.title, st.description + "\n\n" + st.techReqsDescription, c);
			}
		}
	};
	
	//CRAFT: Higher list, radio selector of type of available craftable item
	private final ListEntryCallback GUI_LEC_CRAFTABLE = new ListEntryCallback() {
		@Override
		public void action(int id) {
			craftingDialogState.select(Heartstrings.Craftable.values()[id]);
			for (int i = 13; i <= 22; ++i)
				scrollbars[i].initialized = false;
		}
		
		@Override
		public void entry(Vector2 position, Vector2 size, int id, Color[] color) {
			Craftable ca = Heartstrings.Craftable.values()[id];
			if (Faction.debug.availableCraftables.contains(ca))
				advancedButton(position, size, id, this, color, 
				               Heartstrings.get(ca, Heartstrings.craftableProperties).title, 
				               null, (craftingDialogState.selected == ca) ? GUI.GUI_COLOR_SEVENTH : null);
		}
	};
	
	//CRAFT: Lower list, selects special technologies of selected craftable item
	private final ListEntryCallback GUI_LEC_CST = new ListEntryCallback() {
		@Override
		public void action(int id) {
			craftingDialogState.toggleST(craftingDialogState.availableST[id]);
		}
		
		@Override
		public void entry(Vector2 position, Vector2 size, int id, Color[] color) {
			SpecialTechnology st = craftingDialogState.availableST[id];
			advancedButton(position, size, id, this, color, 
			               Heartstrings.get(st, Heartstrings.stProperties).title, 
			               null, (craftingDialogState.isSTSelected(st)) ? GUI.GUI_COLOR_SEVENTH : null);
		}
	};
	
	//YARD: List on the left side of screen, allows selecting units for upgrading and deploying
	private final ListEntryCallback GUI_LEC_YARD_MANAGEMENT = new ListEntryCallback() {
		@Override
		public void action(int id) {
			Unit u = focusedStruct.yard.get(id);
			
			if (u.state == Unit.State.PARKED)
				yardDialogState.select(u);
			
			for (int i = 25; i <= 30; ++i)
				scrollbars[i].initialized = false;
		}
		
		@Override
		public void entry(Vector2 position, Vector2 size, int id, Color[] color) {
			Unit u = focusedStruct.yard.get(id);
			if (u.state == Unit.State.REPAIRING || u.isDamaged())
				progressbar(position, size, u.condition / u.getMaxCondition(), 
				            u.state == Unit.State.REPAIRING ? GUI.GUI_COLORS_PROGRESS_REPAIRING_TRANSPARENT :
				                                              GUI.GUI_COLORS_PROGRESS_DAMAGED_TRANSPARENT);
			advancedButton(position, size, id, this, color, 
			               (yardDialogState.lastChecked == u ? ">" : "") + u.name, null/*TODO: prompt*/, 
			               yardDialogState.selectedUnitsForDeployment.contains(u) ? GUI.GUI_COLOR_SEVENTH : 
			                                                                        u.state == Unit.State.UPGRADING ? Color.CYAN : null);
		}
	};
	
	//YARD: List of ST available to add before upgrading unit
	private final ListEntryCallback GUI_LEC_YM_ST = new ListEntryCallback() {
		@Override
		public void action(int id) {
			SpecialTechnology st = yardDialogState.availableST[id];
			//if (!yardDialogState.lastChecked.st.contains(st))
				yardDialogState.checkST(st);
		}
		
		@Override
		public void entry(Vector2 position, Vector2 size, int id, Color[] color) {
			SpecialTechnologyProperties stp = Heartstrings.get(yardDialogState.availableST[id], Heartstrings.stProperties);
			if (focusedStruct.ownerFaction.isInvestigated(yardDialogState.availableST[id]))
				if(!yardDialogState.lastChecked.st.contains(yardDialogState.availableST[id]))
					advancedButton(position, size, id, this, color, 
					               stp.title, stp.description, 
					               yardDialogState.stToAdd.contains(yardDialogState.availableST[id]) ? GUI.GUI_COLOR_SEVENTH : null);
				else
					advancedButton(position, size, id, GUI_ACT_DUMMY, GUI_COLORS_TRANSPARENT, 
					               stp.title, stp.description, GUI.GUI_COLOR_SEVENTH);
		}
	};
	
	//TRADE: Resource selector on the left
	private final ListEntryCallback GUI_LEC_TRADE_RESOURCES = new ListEntryCallback() {
		@Override
		public void action(int id) {
			Resource resource = Resource.values()[id];
			tradeDialogState.selectedResource = resource;
		}
		
		@Override
		public void entry(Vector2 position, Vector2 size, int id, Color[] color) {
			Resource resource = Resource.values()[id];
			advancedButton(position, size, id, this, color, 
			               resource.name(), null, 
			               tradeDialogState.selectedResource == resource ? GUI.GUI_COLOR_SEVENTH : null);
		}
	};
	
	//TRADE: Transporters list on the right
	private final ListEntryCallback GUI_LEC_TRADE_TRANSPORTERS = new ListEntryCallback() {
		@Override
		public void action(int id) {}
		
		@Override
		public void entry(Vector2 position, Vector2 size, int id, Color[] color) {
			Unit u =  TradeDialog.getTransporterByID(focusedSquad, id);
			advancedButton(position, size, id, this, color, 
			               u.name, null, null);
		}
	};

	private final Callback GUI_ACT_DUMMY = new Callback(){
		@Override
		public void action(int id) {}
	};
	
	private final Callback GUI_ACT_CRAFTING_ORDER = new Callback(){
		@Override
		public void action(int id) {
			//scrollbars[22].offset; <-- amount of ordered units
			if (scrollbars[22].offset == 0)
				return;
			focusedStruct.orderCrafting(craftingDialogState.selected, scrollbars[22].offset, 
			                            craftingDialogState.selectedT, craftingDialogState.selectedST);
			scrollbars[22].offset = 0;
		}
	};

	private final Callback GUI_ACT_DEPLOY = new Callback(){
		@Override
		public void action(int id) {
			WG.antistatic.gui.focusedStruct.deploySquad(yardDialogState.selectedUnitsForDeployment);
			yardDialogState.reset();
		}
	};
	
	private final Callback GUI_ACT_DEPLOY_ALL = new Callback(){
		@Override
		public void action(int id) {
			for (int i = 0; i < focusedStruct.yard.size(); ++i)
				if (!yardDialogState.selectedUnitsForDeployment.contains(focusedStruct.yard.get(i)))
					GUI_LEC_YARD_MANAGEMENT.action(i);
			GUI_ACT_DEPLOY.action(-1);
		}
	};
	
	private final Callback GUI_ACT_REPAIR = new Callback(){
		@Override
		public void action(int id) {
			Unit u = yardDialogState.lastChecked;
			if (u.state == Unit.State.REPAIRING)
				focusedStruct.cancelRepairing(u);
			else 
				if (yardDialogState.lastChecked.canBeRepaired(focusedStruct)){
					if (yardDialogState.selectedUnitsForDeployment.contains(u))
						yardDialogState.selectedUnitsForDeployment.remove(u);
					focusedStruct.orderRepairing(u);
				} else {
					System.out.println("Not enough resources");
				}
		}
	};
	
	/**Collects offsets from scrollbars assigned to YM's upgrade dialog and transforms them into techLevel array
	 * 
	 * @return
	 */
	private float[] guiYMHarvestUpgradeNTData(){
		Unit u = yardDialogState.lastChecked;
		float[] nt = {0, 0, 0, 0, 0, 0};
		for (int i = 25; i <= 30; ++i)
			if (guiYMgetTUpgradeSBStates(i - 25) == 1) //In case unit tech equals fraction tech
				nt[i - 25] = u.techLevel[i - 25];
			else
				nt[i - 25] = Utils.remap(scrollbars[i].offset, 0, guiYMgetTUpgradeSBStates(i - 25) - 1, 
				                         u.techLevel[i - 25], focusedStruct.ownerFaction.tech[i - 25]);
		return nt;
	}
	
	/**Calculates amount of states for technology scrollbars' update() for YM's upgrade dialog
	 * 
	 * @return
	 */
	private int guiYMgetTUpgradeSBStates(int i){
		return (int)Math.floor((focusedStruct.ownerFaction.tech[i] - yardDialogState.lastChecked.techLevel[i]) * 100f) + 1;
	}
	
	private final Callback GUI_ACT_UPGRADE = new Callback(){
		@Override
		public void action(int id) {
			Unit u = yardDialogState.lastChecked;
			float[] nt = guiYMHarvestUpgradeNTData();
			
			if (focusedStruct.resources.get(Resource.METAL) >= Heartstrings.getUpgradeCostInMetal(u, nt, yardDialogState.stToAdd)){
				if (yardDialogState.selectedUnitsForDeployment.contains(u))
					yardDialogState.selectedUnitsForDeployment.remove(u);
				focusedStruct.orderUprgade(u, nt, yardDialogState.stToAdd);
			}
		}
	};
	
	public Structure focusedStruct;
	public Squad focusedSquad;
	private Aligner aligner;
	private CraftingDialog craftingDialogState = new CraftingDialog();
	private YardDialog yardDialogState = new YardDialog();
	private TradeDialog tradeDialogState = new TradeDialog();
	public void dialog(WG.Dialog dialog){
		//Drawing dialog backround and close button
		sr.setColor(GUI_COLORS_DEFAULT[0]);
		sr.rect(DIM_DIALOG_REFPOINT.x, DIM_DIALOG_REFPOINT.y, DIM_DIALOG_SIZE.x, DIM_DIALOG_SIZE.y);
		button(DIM_BUTTON_POS_CLOSE, DIM_BUTTON_SIZ_CLOSE, -1, GUI_ACT_BUTTON_CLOSE, GUI_COLORS_BUTTON_CLOSE);
		
		String dialogTitle = "NO TITLE SET";
		//Drawing layout
		switch (dialog){
		case STATS:
			dialogTitle = "Statistics and data";
			aligner.setSize(.4f, .9f);
			aligner.next(0, 1);
			caption(aligner.position, "Faction" + focusedStruct.ownerFaction.name, font, VALIGN_TOP, null);
			aligner.next(1, 0);
			aligner.setSize(.6f, .1f);
			caption(aligner.position, "TYPE: " + focusedStruct.type.name(), font, VALIGN_TOP, null);
			for (Resource r: Resource.values()){
				aligner.next(0, -1);
				caption(aligner.position, r.name() + ": " + focusedStruct.resources.get(r), font, VALIGN_TOP, null);
			}
			break;
		case LABORATORY:
			dialogTitle = "Laboratory";
			aligner.setSize(.4f, 1f);
			list(aligner.position, aligner.size, Heartstrings.stProperties.length, GUI_LEC_ST, GUI_COLORS_DEFAULT, 0);
			aligner.next(1, 1);
			aligner.setSize(.4f, .1f);
			aligner.next(0, -1);
			float captionCenteringOffset = (aligner.size.y - font.getCapHeight()/*glay.height*/) / 2f;
			for (int i = 0; i < Heartstrings.Technology.values().length; ++i){
				if (!scrollbars[1 + i].initialized){
					scrollbars[1 + i].offset = Faction.debug.techPriorities[i];
					scrollbars[1 + i].init(Utils.getVector(aligner.position), 
					                       Utils.getVector(aligner.size), 
					                       false, .5f);
				}
				scrollbars[1 + i].update(Faction.MAXPRIOR);
				scrollbars[1 + i].render(GUI_COLORS_SCROLLBAR_COLORS);
				progressbar(aligner.position, aligner.size, 
				            Faction.debug.getRelativeInvestigationPriority(Technology.values()[i]) * Faction.debug.investition,
				            GUI_COLORS_SEVENTH_PROGRESS_TRANSPARENT);
				caption(Utils.getVector(aligner.position).add(3f, captionCenteringOffset), 
				        Heartstrings.tProperties[i].title, font, VALIGN_BOTTOM, null);
				Faction.debug.techPriorities[i] = scrollbars[1 + i].offset;
				
				aligner.next(1, 0);
				float centeringOffset = aligner.size.y / 2f;
				circledProgressbar(Utils.getVector(aligner.position).add(centeringOffset * 1.5f, centeringOffset), centeringOffset * 1.22f,
				                   Faction.debug.techLevel(Technology.values()[i]), GUI_COLOR_SEVENTH);
				aligner.next(-1, -1);
			}
			if (!scrollbars[23].initialized){
				scrollbars[23].offset = Math.round(Faction.debug.investition * Scrollbar.GUI_SB_DEFAULT_STATES);
				scrollbars[23].init(Utils.getVector(aligner.position), 
				                    Utils.getVector(aligner.size), 
				                    false, .5f);
			}
			scrollbars[23].update(Scrollbar.GUI_SB_DEFAULT_STATES);
			scrollbars[23].render(GUI_COLORS_SCROLLBAR_COLORS);
			caption(Utils.getVector(aligner.position).add(3f, captionCenteringOffset), 
			        "Investition", font, VALIGN_BOTTOM, null);
			Faction.debug.investition = scrollbars[23].offset / ((float)Scrollbar.GUI_SB_DEFAULT_MAXVAL);
			
			aligner.next(0, -1);
			caption(aligner.position, String.format("Science data available: %.2f", Faction.debug.scienceDataAvailable), font, VALIGN_BOTTOM, null);
			break;
		case CRAFTING:
			dialogTitle = "Assembly Factory";
			aligner.setSize(.4f, .5f);
			list(aligner.position, aligner.size, craftingDialogState.availableST.length, GUI_LEC_CST, GUI_COLORS_DEFAULT, 11);
			aligner.next(0, 1);
			list(aligner.position, aligner.size, Heartstrings.Craftable.values().length, GUI_LEC_CRAFTABLE, GUI_COLORS_DEFAULT, 12);
			aligner.next(1, 1);
			aligner.setSize(.3f, .1f);
			aligner.next(0, -1);
			for (int i = 0; i < Heartstrings.Technology.values().length; ++i){
				if (!scrollbars[13 + i].initialized)
					scrollbars[13 + i].init(Utils.getVector(aligner.position), 
					                       Utils.getVector(aligner.size), 
					                       false, Scrollbar.GUI_SB_DEFAULT_THUMB);
				if (Utils.arrayContains(Heartstrings.get(craftingDialogState.selected, Heartstrings.craftableProperties).availableTechs, 
				                        Heartstrings.Technology.values()[i])){
					scrollbars[13 + i].update((int)Math.floor(focusedStruct.ownerFaction.tech[i] * 100f) + 1);
					scrollbars[13 + i].render(GUI_COLORS_SCROLLBAR_COLORS);
					craftingDialogState.selectedT[i] = scrollbars[13 + i].offset / 100f;
					
					aligner.next(1, 0);
					caption(aligner.position, 
					        String.format(Heartstrings.tProperties[i].shortTitle + " %6.2f%%", 
					                      craftingDialogState.selectedT[i] * 100f), 
					        font, VALIGN_BOTTOM, null);
					aligner.next(-1, -1);
				}
			}
			aligner.next(0, -1);
			if (!scrollbars[22].initialized)
				scrollbars[22].init(aligner.position, aligner.size, false, Scrollbar.GUI_SB_DEFAULT_THUMB);
			scrollbars[22].update(Heartstrings.getMaxCraftingOrder(craftingDialogState.selected, focusedStruct, 
			                                                       craftingDialogState.selectedT, craftingDialogState.selectedST) + 1);
			scrollbars[22].render(GUI_COLORS_SCROLLBAR_COLORS);
			aligner.next(1, 0);
			caption(aligner.position, "Order: " + scrollbars[22].offset, font, VALIGN_BOTTOM, null);
			aligner.next(-1, 0);
			aligner.setSize(.6f, .12f);
			aligner.next(0, -1);
			
			float price = Heartstrings.getCraftingCost(craftingDialogState, Resource.METAL, 1);
			int amount = scrollbars[22].offset;
			
			caption(aligner.position, "Price: " + price + "M × " + amount + " = " + (price * amount) + "M", 
			        font, VALIGN_BOTTOM, null);
			aligner.next(0, -1);
			advancedButton(aligner.position, aligner.size, -1, GUI_ACT_CRAFTING_ORDER, 
			               GUI_COLORS_DEFAULT, "Place an order", null, null);
			break;
		case YARD:
			dialogTitle = "Vehicle Yard";
			aligner.setSize(.4f, .1f);
			advancedButton(aligner.position, aligner.size, -1, GUI_ACT_DEPLOY, 
			               GUI_COLORS_DEFAULT, "Deploy", null, null);
			aligner.next(0, 1);
			advancedButton(aligner.position, aligner.size, -1, GUI_ACT_DEPLOY_ALL, 
		               GUI_COLORS_DEFAULT, "Deploy All", null, null);
			aligner.next(0, 1);
			aligner.setSize(.4f, .7f);
			list(aligner.position, aligner.size, focusedStruct.yard.size(), GUI_LEC_YARD_MANAGEMENT, GUI_COLORS_DEFAULT, 24);
			aligner.next(0, 1);
			aligner.setSize(.4f, .1f);
			caption(aligner.position, "Yard", font, VALIGN_BOTTOM, null);
			aligner.reset();
			aligner.setSize(.4f, 1);
			aligner.next(1, 1);
			
			Unit u = yardDialogState.lastChecked;
			if (u != null){
				aligner.setSize(.6f, .1f);
				aligner.next(0, -1);
				caption(aligner.position, u.type.name() + " " + u.name, 
				        font, VALIGN_BOTTOM, null);
				aligner.next(0, -1);
				if (u.isDamaged()){
					caption(aligner.position, 
					        "Condition: " + (u.state == Unit.State.REPAIRING ? "Repairing" : "Damaged"), 
					        font, VALIGN_BOTTOM, null);
					aligner.next(0, -1);
					advancedButton(aligner.position, aligner.size, -1, GUI_ACT_REPAIR, 
					               GUI_COLORS_DEFAULT, u.state == Unit.State.REPAIRING ? "Cancel Repair" : "Repair", null, 
					               yardDialogState.lastChecked.canBeRepaired(focusedStruct) ? null : Color.DARK_GRAY);
					
				} else {
					if (u.state == Unit.State.PARKED) {
						caption(aligner.position, "Upgrade: ", 
						        font, VALIGN_BOTTOM, null);
						aligner.next(0, -1);
						aligner.setSize(.3f, .1f);
						for (int i = 0; i < Heartstrings.Technology.values().length; ++i){
							if (!scrollbars[25 + i].initialized)
								scrollbars[25 + i].init(Utils.getVector(aligner.position), 
								                        Utils.getVector(aligner.size), 
								                        false, Scrollbar.GUI_SB_DEFAULT_THUMB);
							if (Utils.arrayContains(Heartstrings.get(Heartstrings.fromUnitType(yardDialogState.lastChecked.type), 
							                                         Heartstrings.craftableProperties).availableTechs, 
							                        Heartstrings.Technology.values()[i])){
								scrollbars[25 + i].update(guiYMgetTUpgradeSBStates(i));
								scrollbars[25 + i].render(GUI_COLORS_SCROLLBAR_COLORS);
								
								caption(aligner.position, 
								        String.format(Heartstrings.tProperties[i].shortTitle + " %3d/%3d%%", 
								                      scrollbars[25 + i].offset + (int)Math.floor(yardDialogState.lastChecked.techLevel[i] * 100f), (int)Math.floor(focusedStruct.ownerFaction.tech[i] * 100f)), 
								        font, VALIGN_BOTTOM, null);
								aligner.next(0, -1);
							}
						}
						aligner.reset();
						aligner.setSize(.7f, .1f);
						aligner.next(1, 1);
						aligner.setSize(.3f, .7f);
						list(aligner.position, aligner.size, yardDialogState.availableST.length, GUI_LEC_YM_ST, GUI_COLORS_DEFAULT, 31);
						aligner.setSize(.3f, .1f);
						aligner.next(-1, -1);
						aligner.setSize(.6f, .1f);
						advancedButton(aligner.position, aligner.size, -1, GUI_ACT_UPGRADE, 
						               GUI_COLORS_DEFAULT, "Upgrade", null, 
						               yardDialogState.lastChecked.canBeRepaired(focusedStruct) ? null : Color.DARK_GRAY);
						aligner.next(0, 1);
						caption(aligner.position, "Upgrade cost: " + Heartstrings.getUpgradeCostInMetal(u, guiYMHarvestUpgradeNTData(), yardDialogState.stToAdd) + "M",
						        font, VALIGN_BOTTOM, null);
					} else if (u.state == Unit.State.UPGRADING){
						caption(aligner.position, "Upgrading...", 
						        font, VALIGN_BOTTOM, null);
					} else {
						caption(aligner.position, "EVERYTHING IS PLAIN WRONG", 
						        font, VALIGN_BOTTOM, null);
					}
				}
			}
			
			
			break;
		case TRADE:
			dialogTitle = "The economy, stupid";
			
			if (!scrollbars[35].initialized)
				scrollbars[35].init(Utils.getVector(aligner.position), 
				                    Utils.getVector(aligner.size), 
				                    false, Scrollbar.GUI_SB_DEFAULT_THUMB);
			
			aligner.setSize(.3f, .9f);
			list(aligner.position, aligner.size, Resource.values().length, GUI_LEC_TRADE_RESOURCES, GUI_COLORS_DEFAULT, 33);
			aligner.next(0, 1);
			aligner.setSize(.3f, .1f);
			caption(aligner.position, "Structure name", font, VALIGN_BOTTOM, null);
			aligner.reset();
			aligner.shift(.7f, .0f, 1, 0);
			aligner.setSize(.3f, .9f);
			list(aligner.position, aligner.size, TradeDialog.countTransporters(focusedSquad), GUI_LEC_TRADE_TRANSPORTERS, GUI_COLORS_DEFAULT, 34);
			aligner.next(0, 1);
			caption(aligner.position, "Squad name", font, VALIGN_BOTTOM, null);
			aligner.reset();
			
			aligner.shift(.3f, .1f, 1, 9);
			aligner.setSize(.4f, .1f);
			//caption(aligner.position, "Titol text", font, VALIGN_BOTTOM, null);
			aligner.next(0, -1);
			if (tradeDialogState.selectedResource != null){
				scrollbars[35].update((int)Math.floor(tradeDialogState.getMaxLoadable(focusedSquad)));
				scrollbars[35].render(GUI_COLORS_SCROLLBAR_COLORS);
				aligner.next(0, -1);
				caption(aligner.position, "How many resources one side has", font, VALIGN_BOTTOM, null);
//TODO: stopped here
				aligner.next(0, -1);
				caption(aligner.position, "And other", font, VALIGN_BOTTOM, null);
			}
			break;
		case NONE:
			break;
		default:
			break;
		}
		
		aligner.reset();
		showPostponedPrompt();
		caption(normalToUI(Utils.getVector(0, 1f), true).add(DIM_MARGIN.x, (DIM_BUTTON_SIZ_CLOSE.y - font.getCapHeight()) / 2f),
		        dialogTitle, font, VALIGN_BOTTOM, null);
	}
	
	public void menu(ListEntryCallback entry, Color[] colors, int length){
		Vector2 size = Utils.getVector();
		size.x = WG.UI_W * .4f;
		size.y = LIST_ENTRY_HEIGHT * (float)length;
		
		Vector2 position = Utils.getVector();
		position.x = (WG.UI_W - size.x) / 2f;
		position.y = (WG.UI_H - size.y) / 2f;
		
		list(position, size, length, entry, colors, 32);
	}
	
	private static final float SCROLLBAR_RWIDTH = .1f, LIST_ENTRY_HEIGHT = 43f;
	public void list(Vector2 position, Vector2 size, int entries, ListEntryCallback entry, Color[] colors, int scrollID){
		int entriesPerPage = Math.round(size.y / LIST_ENTRY_HEIGHT);
		float entryHeight = size.y / entriesPerPage;
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
			entry.entry(Utils.getVector(position).add(0, size.y - (i - offset + 1) * entryHeight), 
			            Utils.getVector(listW, entryHeight), i, colors);
		}
	}
	
	private static final int PIE_MENU_SECTOR_MARGIN = 5;
	public void piemenu(Vector2 position, float radius, Color unselected, Color selected, ArrayList<PiemenuEntry> pm){
		int selectedIndex = -1;
		for(int i = 0; i < pm.size(); i++){
			float angle = Utils.getAngle(context.getUIFromWorldV(Utils.WorldMousePosition).sub(position));
			if (angle > i * (360 / (float) pm.size()) + PIE_MENU_SECTOR_MARGIN &&
			    angle < (i + 1) * (360 / (float) pm.size()) + PIE_MENU_SECTOR_MARGIN){
				if (Utils.isTouchJustReleased){
					WG.antistatic.uistate = WG.UIState.FREE;
					if (pm.get(i).action != null)
						pm.get(i).action.action(0);
				}
				sr.setColor(selected);
				selectedIndex = i;
			} else
				sr.setColor(unselected);
			Utils.drawTrueArc(sr, position, 20, i * (360 / (float) pm.size()) + PIE_MENU_SECTOR_MARGIN, (360 / (float) pm.size()) - 2 * PIE_MENU_SECTOR_MARGIN, 70);
			if (pm.get(i).caption != null && selectedIndex != -1)
				prompt(GUI_COLORS_DEFAULT[1], pm.get(selectedIndex).caption, Utils.getVector(position).add(0, radius * -2f), true);
		}
	}
	
	private static final int PROMPT_BORDER = 10;
	private boolean postponedPromptIsSet = false;
	private Color postponedPromptColor;
	private String postponedPrompt;
	private void postponePrompt(Color back, String prompt){
		postponedPromptIsSet = true;
		postponedPromptColor = back;
		postponedPrompt = prompt;
	}
	
	private void showPostponedPrompt(){
		if (postponedPromptIsSet){
			prompt(postponedPromptColor, postponedPrompt);
			postponedPromptIsSet = false;
		}
	}
	
	public void prompt(String prompt){
		prompt(GUI_COLORS_DEFAULT[0], prompt);
	}
	public void prompt(Color back, String prompt){
		prompt(back, prompt, Utils.UIMousePosition, false);
	}
	public void prompt(Color back, String prompt, Vector2 position, boolean centered){
		//TODO: Rewrite with VALIGN constants available now
		glay.setText(subFont, prompt);
		sr.setColor(back);
		Vector2 corner;
		if (centered)
			corner = Utils.getVector(position.x - glay.width / 2f - PROMPT_BORDER, 
			                         Math.max(position.y - glay.height / 2f - PROMPT_BORDER, 0));
		else
			corner = Utils.getVector(position.x + 5, Math.max(position.y - glay.height - 5, 0));
		
		sr.rect(corner.x, corner.y, glay.width + PROMPT_BORDER * 2, glay.height + PROMPT_BORDER * 2);
		//caption(corner.add(PROMPT_BORDER, PROMPT_BORDER), prompt, subFont, Align.left, WG.UI_W * .25f, VALIGN_BOTTOM, null);
		caption(corner.add(PROMPT_BORDER, PROMPT_BORDER), prompt, subFont, VALIGN_BOTTOM, null);
	}
	
	public void button(Vector2 position, Vector2 size, int id, Callback callback, Color[] colors){
		Color color = colors[0];
		if (UIMouseHovered(position.x, position.y, size.x, size.y)){
			color = colors[1];
			if (Gdx.input.isTouched())
				color = colors[2];
			if (Utils.confirmedTouchOccured)//(Utils.isTouchJustReleased)
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
			caption(Utils.getVector(position).add(DIM_MARGIN.x, (size.y - glay.height) * .5f), caption, font, VALIGN_BOTTOM, textColor);
		}
		if (prompt != null && UIMouseHovered(position, size)){
			postponePrompt(GUI_COLORS_DEFAULT[0], prompt);
		}
	}
	
	public void progressbar(Vector2 position, Vector2 size, float progress, Color[] style){
		sr.setColor(style[1]);
		sr.rect(position.x, position.y, size.x, size.y);
		sr.setColor(style[0]);
		sr.rect(position.x, position.y, size.x * progress, size.y);
	}
	
	public void circledProgressbar(Vector2 position, float radius, float progress, Color color){
		circledProgressbar(position, radius, 0, progress, color);
	}

	public void circledProgressbar(Vector2 position, float radius, float offset, float progress, Color color){
		sr.setColor(color);
		Utils.drawTrueArc(sr, position, radius, 0, progress * 360, Math.round(progress * 360));
		String caption = String.valueOf(Math.round(progress * 100f));
		glay.setText(subFont, caption);
		caption(Utils.getVector(position).add(glay.width / -2f, glay.height / -2f), 
		        caption, subFont, VALIGN_BOTTOM, null);
	}
	public void path(Vector2[] path, float width, Color color){
		path(path, width, color, 0);
	}
	public void path(Vector2[] path, float width, Color color, int offset){
		if (path.length <= 2)
			return;

		Vector2 currentNode, nextNode = WG.antistatic.getUIFromWorldV(path[offset]);
		for (int i = offset; i < path.length - 1; ++i){
			currentNode = nextNode;
			nextNode = WG.antistatic.getUIFromWorldV(path[i + 1]);
			sr.setColor(color);
			sr.rectLine(currentNode.x, currentNode.y, nextNode.x, nextNode.y, width);
			//sr.circle(currentNode.x, currentNode.y, 2);
		}
	}

	public static final float VALIGN_BOTTOM = 1f;
	public static final float VALIGN_MIDDLE = .5f;
	public static final float VALIGN_TOP = 0f;
	public void caption(Vector2 position, String text, BitmapFont font, float alignToBottom, Color color){
		caption(position, text, font, Align.left, 0, alignToBottom, color);
	}
	public void caption(Vector2 position, String text, BitmapFont font, int halign, float targetWidth, float alignToBottom, Color color){
		sr.flush();
		glay.setText(font, text);
		
		batch.begin();
		if (color == null)
			font.setColor(GUI_COLOR_TEXT_DEF);
		else
			font.setColor(color);
		//font.draw(batch, text, position.x, position.y);
		font.draw(batch, text, position.x, position.y + glay.height * alignToBottom, targetWidth, halign, targetWidth > 0);
		batch.end();

		Gdx.gl.glEnable(GL20.GL_BLEND); //TODO: Investigate and fix the crutch
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
	}
	
	public void drawWorldIconOnHUD(Texture icon, Vector2 position, float rotation, float sideScaled, Color color){
		drawIcon(icon, WG.antistatic.getUIFromWorldV(position), rotation, sideScaled, color);
	}
	
	public void drawIcon(Texture icon, Vector2 position, float rotation, float sideScaled, Color color){
		sr.flush();
		
		batch.begin();
		batch.setColor(color);
		batch.draw(icon, position.x - sideScaled / 2f, position.y - sideScaled / 2f, 
		           sideScaled / 2f, sideScaled / 2f,
		           sideScaled, sideScaled, 
		           1, 1,
		           rotation, 0, 0,
		           icon.getWidth(), icon.getHeight(), 
		           false, false);
		batch.end();
		
		Gdx.gl.glEnable(GL20.GL_BLEND); //TODO: Investigate and fix the crutch
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
		return in.scl(WG.UI_W, DIM_DIALOG_SIZE.y - DIM_BUTTON_SIZ_CLOSE.y).add(isCoordinate ? DIM_DIALOG_REFPOINT : Vector2.Zero);
	}
}
