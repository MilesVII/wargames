package com.milesseventh.wargames;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class WG extends ApplicationAdapter {
	public enum Dialog {
		NONE, STATS, LABORATORY, CRAFTING, YARD, TRADE, MISSILE_EXCHANGE, MISSILE_DEPLOY
	}
	public enum UIState {
		FREE, DIALOG, PIEMENU, MENU, MOVINGORDER, MISSILE_LAUNCH
	}
	
	//Game constants
	public static/* final*/ int WORLD_W = 700, WORLD_H = 700,
	                        UI_W, UI_H;
	//public static final int UI_H_DEF, UI_W_DEF = UI_H_DEF = 700;
	public static final int MARCHING_STEP = 4;
	public static final float PATHFINDER_DEFAULT_STEP = 6;
	public static final float MINIMAL_WALKABLE_AREA = 1200; //Relative to pathfinder step
	public static final float CAM_ZOOM_MIN = .2f,
	                          CAM_ZOOM_STEP = .02f;
	public static final int VIRTUAL_FRACTION_SIZE = 1000;
	
	//Dimensions
	public static final float STRUCTURE_ICON_RADIUS = 12,
	                          PIE_MENU_RADIUS = 22,
	                          DIALOG_HEIGHT = .8f,
	                          ICON_SIDE = 32,
	                          STRUCTURE_DEPLOYMENT_SPREAD_MIN = 10;
	
	//Variables
	public static WG antistatic;
	private Batch batch, GUIBatch;
	public HeightMap map, oilMap, oreMap;
	public OrthographicCamera camera;
	private ShapeRenderer sr, hsr; 
	private Marching landOutline, unitsOutline;
	private Texture heightTexture, oilTexture, oreTexture, marchT;
	//private Pathfinder landWalker;
	private BitmapFont font;
	private boolean preTouched = false;
	public GUI gui;
	public UIState uistate = UIState.FREE;
	private Piemenuable focusedObject = null; // no pie menu opened if null
	private Dialog currentDialog = Dialog.NONE;
	private float prevPitchGestureDistance = -1;
	private ListEntryCallback menuLEC;
	private int menuLength;
	
	private float loadingProgress = 0;
	private long dtholder;//(float)(System.currentTimeMillis()
	public synchronized void updateLoadingBar(float set){
		loadingProgress = set;
	}
	
	@Override
	public void create () {
		antistatic = this;
		//htowDisplayRatio = Gdx.graphics.getDisplayMode().height / (float) Gdx.graphics.getDisplayMode().width;
		//htowDisplayRatio = Gdx.graphics.getHeight() / (float) Gdx.graphics.getWidth();
		
		FreeTypeFontGenerator ftfg = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Prototype.ttf"));
		FreeTypeFontParameter parameter = new FreeTypeFontParameter();
		parameter.size = 22;
		parameter.characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz:0123456789.,-<>!?/%\"'&×=+[]";
		
		font = ftfg.generateFont(parameter);
		font.setColor(Color.WHITE);
		
		gui = new GUI(this);
		parameter.size = 17;
		gui.subFont = ftfg.generateFont(parameter);
		ftfg.dispose();
		
		GUIBatch = new SpriteBatch();
		GUIBatch.enableBlending();
		hsr = new ShapeRenderer(); 
		sr = new ShapeRenderer(); 
		batch = new SpriteBatch();
		
		
		//unitsOutline = new Marching(t, map.getSize(), MARCHING_STEP, Marching.Mode.RAW);
		
		//landWalker = new Pathfinder(map, 4);
		//landWalker.isAccessible(new Vector2(20, 20),  new Vector2(40, 40));
	}

	public void resize(int width, int height) {
		if (width >= height){
			camera = new OrthographicCamera(WORLD_W, WORLD_W * height / (float)width);
		} else {
			camera = new OrthographicCamera(WORLD_H * width / (float)height, WORLD_H);
		}

		UI_W = width;
		UI_H = height;

		
		camera.translate(WORLD_W / 2.0f, WORLD_H / 2.0f);
		
		OrthographicCamera hudCamera = new OrthographicCamera(UI_W, UI_H);
		
		hudCamera.combined.translate(-UI_W/2f, -UI_H/2f, 0);
		
		GUIBatch.setProjectionMatrix(hudCamera.combined);
		GUIBatch.enableBlending();
		hsr.setProjectionMatrix(hudCamera.combined);

		gui.init(width >= height ? UI_H * .00001f : UI_W * .00001f);
		gui.batch = GUIBatch;
		gui.sr = hsr;
		gui.font = font;

		if (loadingProgress == 0){
			Thread mapInit = new Thread(new Runnable(){
				@Override
				public void run() {
					map = new HeightMap(new Vector2(WORLD_W, WORLD_H), HeightMap.DEFAULT_MAIN_SCHEME, true);
					System.out.println("Map generated in " + (System.currentTimeMillis() - dtholder) + "ms");
					landOutline = new Marching(map, map.getSize(), MARCHING_STEP, Marching.Mode.PRERENDERED);
					oilMap = new HeightMap(new Vector2(WORLD_W, WORLD_H), HeightMap.DEFAULT_OIL_SCHEME, false);
					oreMap = new HeightMap(new Vector2(WORLD_W, WORLD_H), HeightMap.DEFAULT_ORE_SCHEME, false);
					loadingProgress = -1;
				}
			});
			dtholder = System.currentTimeMillis();
			mapInit.start();
		}
	}
	
	private static final float LOADINGBAR_W = .7f, LOADINGBAR_H = .05f;
	@Override
	public void render () {
		//Cursor coordinates update
		Utils.UIMousePosition.x = getUIMouseX(); Utils.UIMousePosition.y = getUIMouseY();
		Utils.WorldMousePosition.x = getWorldMouseX(); Utils.WorldMousePosition.y = getWorldMouseY();
		Utils.isTouchJustReleased = (preTouched != Gdx.input.isTouched() && !Gdx.input.justTouched());//&& pretouched == true instead?
		preTouched = Gdx.input.isTouched();
		if (Gdx.input.justTouched()){
			Utils.UIEnteringTapPosition.x = getUIMouseX(); Utils.UIEnteringTapPosition.y = getUIMouseY();
		}
		Utils.confirmedTouchOccured = Utils.isTouchJustReleased && Utils.UIEnteringTapPosition.dst(getUIMouseX(), getUIMouseY()) < Utils.confirmationTapDistance;
		
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		if (loadingProgress >= 0){
			hsr.setAutoShapeType(true);
			hsr.begin(ShapeType.Filled);
			hsr.setColor(218/255f, 64/255f, 0, 1);
			hsr.rect(UI_W * (1 - LOADINGBAR_W) / 2f, UI_H * (1 - LOADINGBAR_H) / 2f, LOADINGBAR_W * loadingProgress * UI_W, LOADINGBAR_H * UI_H);
			hsr.set(ShapeType.Line);
			hsr.setColor(Color.BLACK);
			hsr.rect(UI_W * (1 - LOADINGBAR_W) / 2f, UI_H * (1 - LOADINGBAR_H) / 2f, LOADINGBAR_W * UI_W, LOADINGBAR_H * UI_H);
			hsr.end();
			return;
		} else if (loadingProgress == -1){//Map generated
			heightTexture = new Texture(map.getPixmap());
			oilTexture = new Texture(oilMap.getPixmap());
			oreTexture = new Texture(oreMap.getPixmap());
			marchT = new Texture(landOutline.getRendered());
			new Faction(GUI.GUI_COLOR_SEVENTH, "Seventh, inc", Utils.debugFindAPlaceForStructure(map));
			loadingProgress = -2;
		};
		
		camera.update();
		sr.setProjectionMatrix(camera.combined);
		batch.setProjectionMatrix(camera.combined);
		
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		
		batch.enableBlending();
		batch.begin();
		batch.setColor(Color.WHITE);
		batch.draw(heightTexture, 0, 0);
		if (Gdx.input.isKeyPressed(Input.Keys.R)){
			batch.draw(oilTexture, 0, 0);
			batch.draw(oreTexture, 0, 0);
		}
		//batch.draw(_marchT, 0, 0);
		batch.end();

		hsr.begin(ShapeType.Filled);
		sr.begin(ShapeType.Line);
		if (uistate == UIState.MOVINGORDER){
			Vector2[] path = Pathfinder.convertNodeToPath(Pathfinder.findPath(map, PATHFINDER_DEFAULT_STEP, 
			                                                                  ((Squad)focusedObject).position, Utils.WorldMousePosition));
			if (path != null)
				gui.path(path, 2, Color.BLACK);
			if (Utils.confirmedTouchOccured){
				Structure s = Utils.findNearestStructure(Faction.debug, Utils.WorldMousePosition, null);
				boolean structureCollision = (s != null && s.position.dst2(Utils.WorldMousePosition) <= Heartstrings.STRUCTURE_INTERACTION_COLLISION_DISTANCE2);
				if (path != null && !structureCollision)
					((Squad)focusedObject).setPath(path);
				uistate = UIState.FREE;
			}
		}
		if (uistate == UIState.MISSILE_LAUNCH){
			Structure silo = (Structure)focusedObject;
			Missile m = silo.getFirstMountedMissile();
			
			sr.setColor(GUI.GUI_COLOR_SEVENTH);
			sr.line(focusedObject.getWorldPosition(), Utils.WorldMousePosition);
			sr.circle(Utils.WorldMousePosition.x, Utils.WorldMousePosition.y, m.getBlastRadius());
			sr.setColor(Color.RED);
			sr.circle(Utils.WorldMousePosition.x, Utils.WorldMousePosition.y, m.getTotalDestructionRadius());
			
			int fuelNeeded = silo.checkFuelNeededForMissileLaunch(Utils.WorldMousePosition);
			int fuelAvailable = silo.resources.get(Resource.FUEL);
			gui.prompt("Fuel needed: " + (fuelNeeded / WG.VIRTUAL_FRACTION_SIZE) + 
			           "\nFuel available: " + (fuelAvailable / WG.VIRTUAL_FRACTION_SIZE) +
			           (fuelNeeded <= fuelAvailable ? "\nLaunch allowed" : ""));
			
			if (Utils.confirmedTouchOccured){
				silo.requestMissileLaunch(Utils.WorldMousePosition);
				uistate = UIState.FREE;
			}
		}
		
		update();
		
		for (Faction runhorsey: Faction.factions){
			for (Structure neverlookback: runhorsey.structs){
				hsr.setColor(Faction.debug.factionColor);
				gui.drawWorldIconOnHUD(neverlookback.getIcon(), neverlookback.position, 0, ICON_SIDE, runhorsey.factionColor);
				gui.circledProgressbar(neverlookback.position, PIE_MENU_RADIUS * 1.5f, neverlookback.condition / neverlookback.getMaxCondition(), runhorsey.factionColor);
				sr.setColor(Color.DARK_GRAY);
				sr.circle(neverlookback.position.x, neverlookback.position.y, Heartstrings.get(neverlookback.type, Heartstrings.structureProperties).fightingRange);
				sr.circle(neverlookback.position.x, neverlookback.position.y, (float)Math.sqrt(Heartstrings.INTERACTION_DISTANCE2));
			}
			for (Squad marchordie: runhorsey.squads){
				gui.drawWorldIconOnHUD(Faction.SQUAD_ICON, marchordie.position, marchordie.lostDirection, ICON_SIDE, runhorsey.factionColor);
				gui.circledProgressbar(marchordie.position, PIE_MENU_RADIUS * 1.5f, marchordie.getCondition() / marchordie.getMaxCondition(), runhorsey.factionColor);
				sr.setColor(Color.ORANGE);
				sr.circle(marchordie.position.x, marchordie.position.y, marchordie.getMaxAttackRange());
			}
		}
		for (Container standstill: Faction.containers){
			gui.drawWorldIconOnHUD(Faction.ICONS[1], standstill.position, 0, ICON_SIDE, Color.BLACK);
		}
		for (Missile havesomerest: Faction.missilesInAir){
			hsr.setColor(Color.RED);
			hsr.circle(getUIFromWorldX(havesomerest.position.x), 
			           getUIFromWorldY(havesomerest.position.y), 
			           (System.currentTimeMillis() % 500) / 500f * 8f);
		}
		if (uistate == UIState.PIEMENU && focusedObject != null)
			gui.piemenu(getUIFromWorldV(focusedObject.getWorldPosition()), PIE_MENU_RADIUS, Color.BLACK, Color.GREEN, focusedObject.getEntries());
		if (uistate == UIState.DIALOG && currentDialog != Dialog.NONE)
			gui.dialog(currentDialog);
		if (uistate == UIState.MENU && menuLEC != null)
			gui.menu(menuLEC, GUI.GUI_COLORS_DEFAULT, menuLength);
		sr.end();
		hsr.end();
	}
	
	private void update(){
		//Debug controls
		if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT) && Faction.factions.size() == 1){
			if (map.isWalkable(Utils.WorldMousePosition.x, Utils.WorldMousePosition.y)){
				new Faction(Color.MAGENTA, "Starfuckers, inc", Utils.WorldMousePosition);
			}
		}
		
		for (Faction f: Faction.factions){
			f.update(Gdx.graphics.getDeltaTime());
		}
		for (Missile m: Faction.missilesInAir){
			m.move(Gdx.graphics.getDeltaTime());
		}
		for (int i = 0; i < Faction.missilesInAir.size();){
			if (Faction.missilesInAir.get(i).exploded)
				Faction.missilesInAir.remove(i);
			else
				++i;
		}
		for (int i = 0; i < Faction.containers.size();){
			Faction.containers.get(i).lifetimeInSeconds -= Gdx.graphics.getDeltaTime();
			if (Faction.containers.get(i).lifetimeInSeconds <= 0)
				Faction.containers.remove(i);
			else
				++i;
		}
		
		//Camera controls
		if (!Gdx.input.isTouched())
			prevPitchGestureDistance = -1;
		if (uistate == UIState.FREE || uistate == UIState.MOVINGORDER || uistate == UIState.MISSILE_LAUNCH){
			if (Gdx.input.isKeyPressed(Input.Keys.A)){
				//Zoom in
				camera.zoom = Math.max(camera.zoom -= CAM_ZOOM_STEP, CAM_ZOOM_MIN);
			}
			if (Gdx.input.isKeyPressed(Input.Keys.Z)){
				//Zoom out
				camera.zoom = Math.min(camera.zoom += CAM_ZOOM_STEP, 1);
			}
			if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
				camera.translate(-2, 0);
			if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
				camera.translate(2, 0);
			if (Gdx.input.isKeyPressed(Input.Keys.DOWN))
				camera.translate(0, -2);
			if (Gdx.input.isKeyPressed(Input.Keys.UP))
				camera.translate(0, 2);
			
			//Camera gestures
			if (Gdx.input.isTouched(0))
				if (Gdx.input.isTouched(1)){
					if (prevPitchGestureDistance != -1)
						camera.zoom += (prevPitchGestureDistance - Utils.getVector(getUIMouseX(1), getUIMouseY(1)).sub(getUIMouseX(0), getUIMouseY(0)).len()) / (float)UI_W;
					prevPitchGestureDistance = Utils.getVector(getUIMouseX(1), getUIMouseY(1)).sub(getUIMouseX(0), getUIMouseY(0)).len();
				} else
					camera.translate(Gdx.input.getDeltaX() * -1 * camera.zoom, Gdx.input.getDeltaY() * camera.zoom);
		}
		//Camera parameters clamping
		camera.zoom = MathUtils.clamp(camera.zoom, CAM_ZOOM_MIN, 1);
		camera.position.x = MathUtils.clamp(camera.position.x, camera.viewportWidth  / 2.0f * camera.zoom, WORLD_W - camera.viewportWidth  / 2.0f * camera.zoom);
		camera.position.y = MathUtils.clamp(camera.position.y, camera.viewportHeight / 2.0f * camera.zoom, WORLD_H - camera.viewportHeight / 2.0f * camera.zoom);
	}
	
	@Override
	public void dispose () {
		sr.dispose();
		batch.dispose();
		map.getPixmap().dispose();
		marchT.dispose();
		heightTexture.dispose();
		oreTexture.dispose();
		oilTexture.dispose();
	}

	public void openDialog(Dialog dialog){
		uistate = UIState.DIALOG;
		currentDialog = dialog;
	}

	public void setFocusOnPiemenuable(Piemenuable pma){
		if (uistate != UIState.FREE)
			return;
		focusedObject = pma;
		uistate = UIState.PIEMENU;
		if (pma.getClass().isAssignableFrom(Structure.class))
			gui.focusedStruct = (Structure) pma;
		else {
			assert(pma.getClass().isAssignableFrom(Squad.class));
			gui.focusedSquad = (Squad) pma;
		}
	}
	
	public void setMenu(ListEntryCallback lec, int length){
		if (uistate != UIState.FREE && uistate != UIState.PIEMENU)
			return;
		uistate = UIState.MENU;
		
		menuLEC = lec;
		menuLength = length;
	}
	
	private float getWorldMouseX(){
		//Our goal is to project coordinates related to top-left corner of window to bottom-left of our world
		return (Gdx.input.getX()) / (float) Gdx.graphics.getWidth()//Here we have normalized cursor position related to camera view but not to world coordinates
				* (camera.viewportWidth * camera.zoom)//We multiply normalized coordinate by width of camera view related to world coordinates
				+ camera.position.x - camera.viewportWidth / 2.0f * camera.zoom;//And then we add position of camera
	}
	
	private float getWorldMouseY(){
		return (Gdx.graphics.getHeight() - Gdx.input.getY()) / (float) Gdx.graphics.getHeight()
				* (camera.viewportHeight * camera.zoom)
				+ camera.position.y - camera.viewportHeight / 2.0f * camera.zoom;
	}

	private float getUIMouseX(){
		//Here we only do the first stage of conversion.
		return (Gdx.input.getX()) / (float) Gdx.graphics.getWidth() * UI_W;//Here we have cursor position related to camera view but not to world coordinates
	}
	
	private float getUIMouseY(){
		return (Gdx.graphics.getHeight() - Gdx.input.getY()) / (float) Gdx.graphics.getHeight() * UI_H;
	}
	
	private float getUIMouseX(int p){
		//Here we only do the first stage of conversion.
		return (Gdx.input.getX(p)) / (float) Gdx.graphics.getWidth() * UI_W;//Here we have cursor position related to camera view but not to world coordinates
	}
	
	private float getUIMouseY(int p){
		return (Gdx.graphics.getHeight() - Gdx.input.getY(p)) / (float) Gdx.graphics.getHeight() * UI_H;
	}

	public float getUIFromWorldX(float x){
		return (x - camera.position.x + camera.viewportWidth  / 2.0f * camera.zoom) / (camera.viewportWidth  * camera.zoom) * UI_W;
	}
	
	public float getUIFromWorldY(float y){
		return (y - camera.position.y + camera.viewportHeight / 2.0f * camera.zoom) / (camera.viewportHeight * camera.zoom) * UI_H;
	}
	
	public Vector2 getUIFromWorldV(Vector2 in){
		return Utils.getVector(getUIFromWorldX(in.x), getUIFromWorldY(in.y));
	}
}
