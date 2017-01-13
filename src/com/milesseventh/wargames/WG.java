package com.milesseventh.wargames;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Blending;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class WG extends ApplicationAdapter {
	public final int FRACTION_GOD = 0,
					 FRACTION_SEVENTH = 1,
					 FRACTION_ID_OFFSET = 2;
    public static final int WORLD_W = 300;
    public static final int WORLD_H = 300;
    public static final int MARCHING_STEP = 4;
    public static final float CAM_ZOOM_MIN = .2f,
    						  CAM_ZOOM_STEP = .02f;
	
	private SpriteBatch batch; 
	private Batch hudBatch;
	NoisedMap map;
    private OrthographicCamera camera;
	private Viewport viewport;
    private ShapeRenderer sr; 
    private Territory t = new Territory(FRACTION_SEVENTH);
    private Marching landOutline, unitsOutline;
	private Texture _noiseT, _marchT;
	private Pathfinder landWalker;
    private BitmapFont font, shoutFont;
	
	@Override
	public void create () {
		FreeTypeFontGenerator ftfg = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Prototype.ttf"));
		FreeTypeFontParameter parameter = new FreeTypeFontParameter();
		parameter.size = 28;
		parameter.characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz:0123456789.<>!?";
		font = ftfg.generateFont(parameter);
		parameter.size = 50;
		shoutFont = ftfg.generateFont(parameter);
		font.setColor(Color.RED);
		ftfg.dispose();
		
        camera = new OrthographicCamera(WORLD_W, WORLD_H);
        camera.translate(WORLD_W / 2.0f, WORLD_H / 2.0f);
        
        viewport = new FitViewport(WORLD_W, WORLD_H, camera);
		sr = new ShapeRenderer(); 
		sr.setProjectionMatrix(camera.combined);
		//sr.translate(-WORLD_W / 2.0f, -WORLD_H / 2.0f, 0);
		
        batch = new SpriteBatch();
        hudBatch = new SpriteBatch();
        hudBatch.setProjectionMatrix(camera.combined);
		
		Pixmap.setBlending(Blending.None);
		map = new NoisedMap(new Vector2(WORLD_W, WORLD_H), new NoisedMap.ColorScheme(Color.LIME, Color.LIME, Color.BROWN, Color.WHITE));
		landOutline = new Marching(map, map.getSize(), MARCHING_STEP, Marching.Mode.PRERENDERED);
		_marchT = new Texture(landOutline.getRendered());
		unitsOutline = new Marching(t, map.getSize(), MARCHING_STEP, Marching.Mode.RAW);
		
		_noiseT = new Texture(map.getPixmap());
		//landWalker = new Pathfinder(map, 4);
		//landWalker.isAccessible(new Vector2(20, 20),  new Vector2(40, 40));
		sr.setColor(Color.BLACK);
	}
	
	@Override
	public void render () {
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		update();
		
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		
		batch.begin();
		batch.draw(_noiseT, 0, 0);
		batch.draw(_marchT, 0, 0);
		batch.end();
		
		hudBatch.begin();
		font.draw(hudBatch, ""+ /*Gdx.graphics.getFramesPerSecond()*/ camera.zoom, -WORLD_W / 2.0f + 5, -WORLD_H / 2.0f + 32);
		hudBatch.end();
		sr.begin(ShapeType.Filled);
		//landOutline.render(sr);
		//unitsOutline.render(sr);
		sr.end();
	}
	
	private void update(){
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

    public void resize(int width, int height) {
        viewport.update(width, height);
    }
    
	@Override
	public void dispose () {
		sr.dispose();
		batch.dispose();
		map.getPixmap().dispose();
		_marchT.dispose();
		_noiseT.dispose();
	}
}
