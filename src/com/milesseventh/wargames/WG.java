package com.milesseventh.wargames;

import java.util.Random;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class WG extends ApplicationAdapter {
	public final int FRACTION_GOD = 0,
					 FRACTION_SEVENTH = 1,
					 FRACTION_ID_OFFSET = 2;
    public static final int SCREEN_W = 700;
    public static final int SCREEN_H = 700;
    public static final int MARCHING_STEP = 2;
	
	SpriteBatch batch;
	NoisedMap map;
    private Camera camera;
	private Viewport viewport;
    private ShapeRenderer sr; 
    private Territory t = new Territory(FRACTION_SEVENTH);
    private Marching landOutline, unitsOutline;
	private Texture _noiseT, _marchT;
	
	@Override
	public void create () {
        camera = new OrthographicCamera(SCREEN_W, SCREEN_H);
        viewport = new FitViewport(SCREEN_W, SCREEN_H, camera);
		sr = new ShapeRenderer(); 
		sr.setProjectionMatrix(camera.combined);
		sr.translate(-SCREEN_W / 2.0f, -SCREEN_H / 2.0f, 0);
		
        batch = new SpriteBatch();
		batch.setProjectionMatrix(camera.combined);
		
		map = new NoisedMap(new Vector2(SCREEN_W, SCREEN_H));
		landOutline = new Marching(map, map.getSize(), MARCHING_STEP, Marching.Mode.PRERENDERED);
		_marchT = new Texture(landOutline.getRendered());
		unitsOutline = new Marching(t, map.getSize(), MARCHING_STEP, Marching.Mode.RAW);
		
		_noiseT = new Texture(map.getPixmap());
		sr.setColor(Color.BLACK);
	}
	
	@Override
	public void render () {
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		batch.begin();
		batch.draw(_noiseT, -SCREEN_W/2, -SCREEN_H/2);
		batch.draw(_marchT, -SCREEN_W/2, -SCREEN_H/2);
		batch.end();
		System.out.println(Gdx.graphics.getFramesPerSecond());
		sr.begin(ShapeType.Filled);
		//landOutline.render(sr);
		//unitsOutline.renderRaw(sr);
		//Utils.marchLine(map, marchingGrid, MARCHING_STEP, sr);
		//Utils.marchLine(t, tmarchingGrid, MARCHING_STEP, sr);
		sr.end();
	}

    public void resize(int width, int height) {
        viewport.update(width, height);
    }
    
	@Override
	public void dispose () {
		sr.dispose();
		batch.dispose();
	}
}
