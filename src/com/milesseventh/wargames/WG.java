package com.milesseventh.wargames;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Buttons;
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
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.milesseventh.wargames.units.City;

public class WG extends ApplicationAdapter {
	public enum Dialog{
		NONE, UNITS_BUILDING, RESOURCE_MANAGER, UNITS_ASSEMBLY
	}
	public enum UnitsTechnology{
		COLUMN_INTERCEPTION, SIEGE, DEFENCE, MOBILE_ATTACK
	}
	public enum MissilesTechnology{
		WARHEAD_FRAGMENTATION, THERMAL_TRAPS
	}
	
	//Game constants
	public static final int WORLD_W = 2000, WORLD_H = 2000,
	                        UI_W = 700, UI_H = 700;
	public static final int MARCHING_STEP = 4;
	public static final float CAM_ZOOM_MIN = .2f,
	                          CAM_ZOOM_STEP = .02f;
	
	//Dimensions
	public static final float CITY_ICON_RADIUS = 12,
	                          PIE_MENU_RADIUS = 22,
	                          DIALOG_HEIGHT = .8f;
	public static final Color GUI_DIALOG_BGD = new Color(0, 0, 0, .7f);
	
	//GUI constants
	public static final Color[] GUI_BUTTON_DEFAULT_COLORS = {
		new Color(0, 0, 0, .5f), 
		new Color(0, 0, 0, 1), 
		new Color(.5f, .5f, .5f, .8f)
	};
	private static final Vector2 GUI_BUTTON_POS_BUILD = new Vector2(5, 5),//Position
	                             GUI_BUTTON_SIZ_BUILD = new Vector2(UI_W * .05f, UI_W * .05f),//Size
	                             GUI_BUTTON_CNT_BUILD = GUI_BUTTON_POS_BUILD.cpy().add(GUI_BUTTON_SIZ_BUILD.cpy().scl(.5f));//Center
	private static final Runnable GUI_BUTTON_ACT_BUILD = new Runnable(){
		@Override
		public void run() {
			System.out.println("BUILD pressed");
			//WG.antistatic.gpstate = WG.GameplayState.BUILDING;
		}
	};
	public static final Vector2 GUI_BUTTON_SIZ_CLOSE = new Vector2(UI_W * .1f, UI_H * .05f),//Close dialog button
	                            GUI_BUTTON_POS_CLOSE = new Vector2(UI_W, UI_H - (UI_H - UI_H * DIALOG_HEIGHT) / 2).sub(GUI_BUTTON_SIZ_CLOSE),
	                            GUI_BUTTON_CNT_CLOSE = GUI_BUTTON_POS_CLOSE.cpy().sub(GUI_BUTTON_SIZ_CLOSE.cpy().scl(.5f));
	public static final Runnable GUI_BUTTON_ACT_CLOSE = new Runnable(){
		@Override
			public void run() {
			WG.antistatic.currentDialog = WG.Dialog.NONE;
		}
	};
	
	//Variables
	public static WG antistatic;
	private SpriteBatch batch; 
	private Batch uiBatch;
	HeightMap map;
	private OrthographicCamera camera;
	private Viewport viewport;
	private ShapeRenderer sr, hsr; 
	//private Territory t = new Territory(FRACTION_SEVENTH);
	private Marching landOutline, unitsOutline;
	private Texture _noiseT, _marchT;
	//private Pathfinder landWalker;
	private BitmapFont font;
	private Matrix4 hudmx;
	private boolean preTouched = false;
	public SessionManager sm;
	public GUI gui;
	//public GameplayState gpstate = GameplayState.DEFAULT;
	private City pieMenuState = null; // null if no pie menu opened
	public Dialog currentDialog = Dialog.NONE;
	@Override
	public void create () {
		antistatic = this;
		
		FreeTypeFontGenerator ftfg = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Prototype.ttf"));
		FreeTypeFontParameter parameter = new FreeTypeFontParameter();
		parameter.size = 17;
		parameter.characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz:0123456789.-<>!?";
		font = ftfg.generateFont(parameter);
		parameter.size = 50;
		
		font.setColor(Color.WHITE);
		ftfg.dispose();

		camera = new OrthographicCamera(WORLD_W, WORLD_H);
		camera.translate(WORLD_W / 2.0f, WORLD_H / 2.0f);
		viewport = new FitViewport(WORLD_W, WORLD_H, camera);
		sr = new ShapeRenderer(); 
		sr.setColor(Color.RED);
		
		batch = new SpriteBatch();
		
		map = new HeightMap(new Vector2(WORLD_W, WORLD_H), new HeightMap.ColorScheme(Color.GREEN, Color.LIME, Color.BROWN, Color.LIME));
		landOutline = new Marching(map, map.getSize(), MARCHING_STEP, Marching.Mode.PRERENDERED);
		_marchT = new Texture(landOutline.getRendered());
		//unitsOutline = new Marching(t, map.getSize(), MARCHING_STEP, Marching.Mode.RAW);
		
		_noiseT = new Texture(map.getPixmap());
		//landWalker = new Pathfinder(map, 4);
		//landWalker.isAccessible(new Vector2(20, 20),  new Vector2(40, 40));
		
		//Game mechanics
		Fraction[] _ = {new Fraction(0, Color.ORANGE, "Seventh, inc", Utils.debugFindAPlaceForCity(map))};
		sm = new SessionManager(_);
		gui = new GUI(this);
	}

	public void resize(int width, int height) {
		viewport.update(width, height);
		
		OrthographicCamera hudCamera = new OrthographicCamera(UI_W, UI_H);
		hudmx = hudCamera.combined;
		hudmx.translate(-UI_W/2f, -UI_H/2f, 0);
		
		uiBatch = new SpriteBatch();
		uiBatch.enableBlending();
		uiBatch.setProjectionMatrix(hudmx);
		hsr = new ShapeRenderer(); 
		hsr.setProjectionMatrix(hudmx);
		hsr.setColor(Color.BLACK);
		gui.batch = uiBatch;
		gui.sr = hsr;
		gui.font = font;
	}
	
	@Override
	public void render () {
		//Cursor coordinates update
		Utils.UIMousePosition.x = getUIMouseX();
		Utils.UIMousePosition.y = getUIMouseY();
		Utils.WorldMousePosition.x = getWorldMouseX();
		Utils.WorldMousePosition.y = getWorldMouseY();
		Utils.isTouchJustReleased = (preTouched != Gdx.input.isTouched() && !Gdx.input.justTouched());
		preTouched = Gdx.input.isTouched();
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		update();
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		sr.setProjectionMatrix(camera.combined);
		
		batch.begin();
		batch.draw(_noiseT, 0, 0);
		//batch.draw(_marchT, 0, 0);
		batch.end();

		hsr.begin(ShapeType.Filled);
		//uiBatch.begin();
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		
		//font.draw(uiBatch, "FONTS", 50, 50);
		//uiBatch.flush();
		
		
		Runnable[] r = {
			new Runnable(){
				@Override
				public void run(){
					System.out.println("Cancelled");
				}
			},
			new Runnable(){
				@Override
				public void run(){
					System.out.println("1");
					currentDialog = Dialog.UNITS_BUILDING;
				}
			},
			new Runnable(){
				@Override
				public void run(){
					System.out.println("2");
				}
			},
			new Runnable(){
				@Override
				public void run(){
					System.out.println("3");
				}
			},
			new Runnable(){
				@Override
				public void run(){
					System.out.println("4");
				}
			}
		};
		//gui.button(hsr, GUI_BUTTON_POS_BUILD, GUI_BUTTON_SIZ_BUILD, GUI_BUTTON_ACT_BUILD, GUI_BUTTON_DEFAULT_COLORS);
		for (Fraction runhorsey: sm.getFractions()){
			for (City neverlookback: runhorsey.getCities()){
				hsr.setColor(runhorsey.getColor());
				//if (neverlookback.getPosition().dst(Utils.WorldMousePosition) < CITY_ICON_RADIUS * 1.2f){
				if (getUIFromWorldV(neverlookback.getPosition()).dst(Utils.UIMousePosition) < CITY_ICON_RADIUS * 1.7f){
					hsr.setColor(runhorsey.getColor().r * 1.2f, runhorsey.getColor().g * 1.2f, runhorsey.getColor().b * 1.2f, 1f);
					if (Gdx.input.justTouched() && currentDialog == Dialog.NONE)
						pieMenuState = neverlookback;
				}
				hsr.circle(this.getUIFromWorldX(neverlookback.getPosition().x), this.getUIFromWorldY(neverlookback.getPosition().y), CITY_ICON_RADIUS);
			}
		}
		if (pieMenuState != null)
			gui.piemenu(hsr, getUIFromWorldV(pieMenuState.getPosition()), PIE_MENU_RADIUS, Color.BLACK, Color.GREEN, r);
		if (currentDialog != Dialog.NONE){
			gui.dialog(currentDialog);
		}
		hsr.end();
		//uiBatch.flush();
		//uiBatch.end();
		if (!Gdx.input.isTouched())
			pieMenuState = null;
	}
	
	private void update(){
		//Debug controls
		if (Gdx.input.isKeyPressed(Input.Keys.M)){
			//Generate new map
			map = new HeightMap(new Vector2(WORLD_W, WORLD_H), new HeightMap.ColorScheme(Color.GREEN, Color.LIME, Color.BROWN, Color.WHITE));
			_noiseT = new Texture(map.getPixmap());
		}
		if (Gdx.input.isKeyPressed(Input.Keys.C)){
			//Try to build a city
			Vector2 _np = new Vector2(getWorldMouseX(), getWorldMouseY());
			if (Utils.debugCheckPlaceForNewCity(map, sm.getCurrent(), _np)){
				sm.getCurrent().registerCity(new City(_np, sm.getCurrent()));
			}
		}
		
		//Camera controls
		if (Gdx.input.isKeyPressed(Input.Keys.A)){
			//Zoom in
			camera.zoom = Math.max(camera.zoom -= CAM_ZOOM_STEP, CAM_ZOOM_MIN);
		}
		if (Gdx.input.isKeyPressed(Input.Keys.Z)){
			//Zoom out
			camera.zoom = Math.min(camera.zoom += CAM_ZOOM_STEP, 1);

			if (camera.position.x - WORLD_W / 2.0f * camera.zoom < 0)
				camera.position.x  = 0 + WORLD_W / 2.0f * camera.zoom;
			if (camera.position.x + WORLD_W / 2.0f * camera.zoom > WORLD_W)
				camera.position.x = WORLD_W - WORLD_W / 2.0f * camera.zoom;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
			if (camera.position.x - WORLD_W / 2.0f * camera.zoom >= 0)
				camera.translate(-2, 0);
		if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
			if (camera.position.x + WORLD_W / 2.0f * camera.zoom < WORLD_W)
				camera.translate(2, 0);
		if (Gdx.input.isKeyPressed(Input.Keys.DOWN))
			if (camera.position.y - WORLD_H / 2.0f * camera.zoom > 0)
				camera.translate(0, -2);
		if (Gdx.input.isKeyPressed(Input.Keys.UP))
			if (camera.position.y + WORLD_H / 2.0f * camera.zoom < WORLD_H)
				camera.translate(0, 2);
	}
	
	@Override
	public void dispose () {
		sr.dispose();
		batch.dispose();
		map.getPixmap().dispose();
		_marchT.dispose();
		_noiseT.dispose();
	}

	private float getWorldMouseX(){
		//Our goal is to project coordinates related to top-right corner of window to bottom-left of our world
		return (Gdx.input.getX() - (Gdx.graphics.getWidth() - viewport.getScreenWidth()) / 2.0f) / viewport.getScreenWidth()//Here we have normalized cursor position related to camera view but not to world coordinates
				* (WORLD_W * camera.zoom)//We multiply normalized coordinate by width of camera view related to world coordinates
				+ camera.position.x - WORLD_W / 2.0f * camera.zoom;//And then we add position of camera
	}
	
	private float getWorldMouseY(){
		return (viewport.getScreenHeight() - Gdx.input.getY() + (Gdx.graphics.getHeight() - viewport.getScreenHeight()) / 2.0f) / viewport.getScreenHeight()
				* (WORLD_H * camera.zoom)
				+ camera.position.y - WORLD_H / 2.0f * camera.zoom;
	}
	
	private float getUIMouseX(){
		//Here we only do the first stage of conversion.
		return (Gdx.input.getX() - (Gdx.graphics.getWidth() - viewport.getScreenWidth()) / 2.0f) / viewport.getScreenWidth() * UI_W;//Here we have cursor position related to camera view but not to world coordinates
	}
	
	private float getUIMouseY(){
		return (viewport.getScreenHeight() - Gdx.input.getY() + (Gdx.graphics.getHeight() - viewport.getScreenHeight()) / 2.0f) / viewport.getScreenHeight() * UI_H;
	}

	public float getUIFromWorldX(float x){
		return (x - camera.position.x + WORLD_W / 2.0f * camera.zoom) / (WORLD_W * camera.zoom) * UI_W;
	}
	
	public float getUIFromWorldY(float y){
		return (y - camera.position.y + WORLD_H / 2.0f * camera.zoom) / (WORLD_H * camera.zoom) * UI_H;
	}
	
	public Vector2 getUIFromWorldV(Vector2 in){
		Vector2 t = in.cpy();
		t.x = getUIFromWorldX(in.x);
		t.y = getUIFromWorldY(in.y);
		return t;
	}
}
