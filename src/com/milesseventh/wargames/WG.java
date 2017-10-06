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
	public final int FRACTION_GOD = 0,
					 FRACTION_SEVENTH = 1,
					 FRACTION_ID_OFFSET = 2;
    public static final int WORLD_W = 700, WORLD_H = 700,
    						HUD_W = 700, HUD_H = 700;
    public static final int MARCHING_STEP = 4;
    public static final float CAM_ZOOM_MIN = .2f,
    						  CAM_ZOOM_STEP = .02f;
	
	private SpriteBatch batch; 
	private Batch hudBatch;
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
	private GUIButton debug_button;
	public SessionManager sm;
	
	
	
	@Override
	public void create () {
		FreeTypeFontGenerator ftfg = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Prototype.ttf"));
		FreeTypeFontParameter parameter = new FreeTypeFontParameter();
		parameter.size = 28;
		parameter.characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz:0123456789.-<>!?";
		font = ftfg.generateFont(parameter);
		parameter.size = 50;
		
		font.setColor(Color.RED);
		ftfg.dispose();

        camera = new OrthographicCamera(WORLD_W, WORLD_H);
        camera.translate(WORLD_W / 2.0f, WORLD_H / 2.0f);
        viewport = new FitViewport(WORLD_W, WORLD_H, camera);
		sr = new ShapeRenderer(); 
		sr.setColor(Color.RED);
		
        batch = new SpriteBatch();
		
		map = new HeightMap(new Vector2(WORLD_W, WORLD_H), new HeightMap.ColorScheme(Color.GREEN, Color.LIME, Color.BROWN, Color.WHITE));
		landOutline = new Marching(map, map.getSize(), MARCHING_STEP, Marching.Mode.PRERENDERED);
		_marchT = new Texture(landOutline.getRendered());
		//unitsOutline = new Marching(t, map.getSize(), MARCHING_STEP, Marching.Mode.RAW);
		
		_noiseT = new Texture(map.getPixmap());
		//landWalker = new Pathfinder(map, 4);
		//landWalker.isAccessible(new Vector2(20, 20),  new Vector2(40, 40));
		
		//Game mechanics
		Fraction[] _ = {new Fraction(FRACTION_SEVENTH, Color.ORANGE, "Seventh, inc", Utils.debugFindAPlaceForCity(map))};
		sm = new SessionManager(_);
		debug_button = new GUIButton(new Vector2(10, 10), new Vector2(HUD_W / 100 * 5, HUD_W / 100 * 5), new GUIButton.GUIEvents(){
			@Override
			public void actionContinious(Object sender) {
				//Do nothing
			}
			
			@Override
			public void action(Object sender) {
				System.out.println("Button pressed");
			}
		});
	}

    public void resize(int width, int height) {
        viewport.update(width, height);
        
        OrthographicCamera hudCamera = new OrthographicCamera(HUD_W, HUD_H);
        hudmx = hudCamera.combined;
        hudmx.translate(-HUD_W/2f, -HUD_H/2f, 0);
        
        hudBatch = new SpriteBatch();
        hudBatch.setProjectionMatrix(hudmx);
		hsr = new ShapeRenderer(); 
		hsr.setProjectionMatrix(hudmx);
		hsr.setColor(Color.BLACK);
    }
	
	@Override
	public void render () {
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
		
		try {
			hudBatch.begin();
			font.draw(hudBatch, "" + map.getMeta(getWorldMouseX(), getWorldMouseY())/*Gdx.graphics.getFramesPerSecond() camera.zoom*/, 50, 50);
			hudBatch.end();
		} catch (Exception e){}

        Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		hsr.begin(ShapeType.Filled);
		if (Gdx.input.isButtonPressed(Buttons.LEFT)){
			hsr.circle(getHUDMouseX(), getHUDMouseY(), 15);
		}
		debug_button.render(hsr);
		//landOutline.render(sr);
		//unitsOutline.render(sr);
		hsr.end();
		
		sr.begin(ShapeType.Filled);
		//sr.circle(getWorldMouseX(), getWorldMouseY(), 5);
		for (Fraction runhorsey: sm.getFractions()){
			//TODO Draw cities as textures
			sr.setColor(runhorsey.getColor());
			for (City neverlookback: runhorsey.getCities())
				Utils.drawCity(sr, neverlookback.getPosition());
		}
		sr.end();
	}
	
	private void update(){
		//Cursor coordinates update
		Utils.HUDMousePosition.x = getHUDMouseX();
		Utils.HUDMousePosition.y = getHUDMouseY();
		Utils.WorldMousePosition.x = getWorldMouseX();
		Utils.WorldMousePosition.y = getWorldMouseY();
		
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
	
	private float getHUDMouseX(){
		//Here we only do the first stage of conversion.
		return (Gdx.input.getX() - (Gdx.graphics.getWidth() - viewport.getScreenWidth()) / 2.0f)/viewport.getScreenWidth()*HUD_W;//Here we have cursor position related to camera view but not to world coordinates
	}
	
	private float getHUDMouseY(){
		return (viewport.getScreenHeight() - Gdx.input.getY() + (Gdx.graphics.getHeight() - viewport.getScreenHeight()) / 2.0f)/viewport.getScreenHeight()*HUD_H;
	}
}
