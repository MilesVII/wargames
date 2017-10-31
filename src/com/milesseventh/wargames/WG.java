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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class WG extends ApplicationAdapter {
	public enum Dialog{
		NONE, LABORATORY
	}
	
	//Game constants
	public static/* final*/ int WORLD_W = 1000, WORLD_H = 1000,
	                        UI_W = 700, UI_H = -1;
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
	private final Croupfuck GUI_BUTTON_ACT_BUILD = new Croupfuck(){
		@Override
		public void action(int source) {
			camera.zoom = Math.max(camera.zoom -= CAM_ZOOM_STEP, CAM_ZOOM_MIN);
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
	private Structure pieMenuState = null; // null if no pie menu opened
	public Dialog currentDialog = Dialog.NONE;
	public static float htowDisplayRatio;
	public float prevPitchGestureDistance = -1;
	
	private float loadingProgress = 0;
	private long dtholder;//(float)(System.currentTimeMillis()
	public synchronized void updateLoadingBar(float set){
		loadingProgress = set;
	}
	
	@Override
	public void create () {
		antistatic = this;
		htowDisplayRatio = Gdx.graphics.getDisplayMode().height / (float) Gdx.graphics.getDisplayMode().width;
		
		FreeTypeFontGenerator ftfg = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Prototype.ttf"));
		FreeTypeFontParameter parameter = new FreeTypeFontParameter();
		parameter.size = 17;
		parameter.characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz:0123456789.-<>!?/%";
		font = ftfg.generateFont(parameter);
		parameter.size = 50;
		
		font.setColor(Color.WHITE);
		ftfg.dispose();

		camera = new OrthographicCamera(WORLD_W, WORLD_W * htowDisplayRatio);
		camera.translate(WORLD_W / 2.0f, WORLD_H / 2.0f);
		viewport = new FitViewport(WORLD_W, WORLD_W * htowDisplayRatio, camera);
		sr = new ShapeRenderer(); 
		sr.setColor(Color.RED);
		UI_H = Math.round(UI_W * htowDisplayRatio);
		
		batch = new SpriteBatch();
		
		Thread mapInit = new Thread(new Runnable(){
			@Override
			public void run() {
				map = new HeightMap(new Vector2(WORLD_W, WORLD_H), HeightMap.DEFAULT_SCHEME);
				System.out.println("Map generated in " + (System.currentTimeMillis() - dtholder) + "ms");
				landOutline = new Marching(map, map.getSize(), MARCHING_STEP, Marching.Mode.PRERENDERED);
				loadingProgress = -1;
			}
		});
		dtholder = System.currentTimeMillis();
		mapInit.start();
		
		//unitsOutline = new Marching(t, map.getSize(), MARCHING_STEP, Marching.Mode.RAW);
		
		//landWalker = new Pathfinder(map, 4);
		//landWalker.isAccessible(new Vector2(20, 20),  new Vector2(40, 40));
		
		//Game mechanics
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
	
	private static final float LOADINGBAR_W = .7f, LOADINGBAR_H = .05f;
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
			_noiseT = new Texture(map.getPixmap());
			_marchT = new Texture(landOutline.getRendered());
			Fraction[] _ = {new Fraction(Color.ORANGE, "Seventh, inc", Utils.debugFindAPlaceForStructure(map))};
			sm = new SessionManager(_);
			loadingProgress = -2;
		};
		
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
		
		
		/*Runnable[] r = {
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
		};*/
		gui.button(GUI_BUTTON_POS_BUILD, GUI_BUTTON_SIZ_BUILD, Utils.NULL_ID, GUI_BUTTON_ACT_BUILD, GUI_BUTTON_DEFAULT_COLORS);
		for (Fraction runhorsey: sm.getFractions()){
			for (Structure neverlookback: runhorsey.getStructs()){
				hsr.setColor(runhorsey.getColor());
				//if (neverlookback.getPosition().dst(Utils.WorldMousePosition) < CITY_ICON_RADIUS * 1.2f){
				if (getUIFromWorldV(neverlookback.getPosition()).dst(Utils.UIMousePosition) < CITY_ICON_RADIUS * 1.7f){
					hsr.setColor(runhorsey.getColor().r * 1.2f, runhorsey.getColor().g * 1.2f, runhorsey.getColor().b * 1.2f, 1f);
					if (Gdx.input.justTouched() && currentDialog == Dialog.NONE){
						gui.currentDialogStruct = pieMenuState = neverlookback;
					}
				}
				hsr.circle(this.getUIFromWorldX(neverlookback.getPosition().x), this.getUIFromWorldY(neverlookback.getPosition().y), CITY_ICON_RADIUS);
			}
		}
		if (pieMenuState != null)
			gui.piemenu(getUIFromWorldV(pieMenuState.getPosition()), PIE_MENU_RADIUS, Color.BLACK, Color.GREEN, pieMenuState.getPieMenuActionsNumber(), Structure.PIEMENU_ACTIONS_CITY);
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
		if (Gdx.input.isKeyPressed(Input.Keys.C)){
			//Try to build a city
			Vector2 _np = new Vector2(getWorldMouseX(), getWorldMouseY());
			if (Utils.debugCheckPlaceForNewStructure(map, sm.getCurrent(), _np)){
				sm.getCurrent().registerStructure(new Structure(_np, Structure.StructureType.CITY, sm.getCurrent()));
			}
		}
		//Debug mechanics
		sm.getFractions()[0].getStructs().get(0).addResource(Structure.Resource.ORE, .001f);
		
		//Camera debug controls
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
		if (!Gdx.input.isTouched())
			prevPitchGestureDistance = -1;
		if (pieMenuState == null && currentDialog == Dialog.NONE && Gdx.input.isTouched(0)){
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
		/* To be deleted
		if (camera.position.x - WORLD_W / 2.0f * camera.zoom < 0)
			camera.position.x = WORLD_W / 2.0f * camera.zoom;
		if (camera.position.x + WORLD_W / 2.0f * camera.zoom > WORLD_W)
			camera.position.x = WORLD_W - WORLD_W / 2.0f * camera.zoom;
		if (camera.position.y - camera.viewportHeight / 2.0f * camera.zoom < 0)
			camera.position.y = camera.viewportHeight / 2.0f * camera.zoom;
		if (camera.position.y + camera.viewportHeight / 2.0f * camera.zoom > WORLD_H)
			camera.position.y = WORLD_H - camera.viewportHeight / 2.0f * camera.zoom;*/
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
		//Our goal is to project coordinates related to top-left corner of window to bottom-left of our world
		return (Gdx.input.getX() - (Gdx.graphics.getWidth() - viewport.getScreenWidth()) / 2.0f) / viewport.getScreenWidth()//Here we have normalized cursor position related to camera view but not to world coordinates
				* (camera.viewportWidth * camera.zoom)//We multiply normalized coordinate by width of camera view related to world coordinates
				+ camera.position.x - camera.viewportWidth / 2.0f * camera.zoom;//And then we add position of camera
	}
	
	private float getWorldMouseY(){
		return (viewport.getScreenHeight() - Gdx.input.getY() + (Gdx.graphics.getHeight() - viewport.getScreenHeight()) / 2.0f) / viewport.getScreenHeight()
				* (camera.viewportHeight * camera.zoom)
				+ camera.position.y - camera.viewportHeight / 2.0f * camera.zoom;
	}

	private float getUIMouseX(){
		//Here we only do the first stage of conversion.
		return (Gdx.input.getX() - (Gdx.graphics.getWidth() - viewport.getScreenWidth()) / 2.0f) / viewport.getScreenWidth() * UI_W;//Here we have cursor position related to camera view but not to world coordinates
	}
	
	private float getUIMouseY(){
		return (viewport.getScreenHeight() - Gdx.input.getY() + (Gdx.graphics.getHeight() - viewport.getScreenHeight()) / 2.0f) / viewport.getScreenHeight() * UI_H;
	}
	
	private float getUIMouseX(int p){
		//Here we only do the first stage of conversion.
		return (Gdx.input.getX(p) - (Gdx.graphics.getWidth() - viewport.getScreenWidth()) / 2.0f) / viewport.getScreenWidth() * UI_W;//Here we have cursor position related to camera view but not to world coordinates
	}
	
	private float getUIMouseY(int p){
		return (viewport.getScreenHeight() - Gdx.input.getY(p) + (Gdx.graphics.getHeight() - viewport.getScreenHeight()) / 2.0f) / viewport.getScreenHeight() * UI_H;
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
