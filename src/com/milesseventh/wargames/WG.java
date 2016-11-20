package com.milesseventh.wargames;

import java.util.Random;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
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
    public static final int SCREEN_W = 3000;
    public static final int SCREEN_H = 3000;
    public static final int MARCHING_STEP = 4;
    public static final int INFINITY = 10000;
	
	SpriteBatch batch;
	NoisedMap map;
    private Camera camera;
	private Viewport viewport;
    private ShapeRenderer sr; 
    private float[][] marchingGrid;
    private Territory t = new Territory(FRACTION_SEVENTH);
	private Texture _noiseT;
	
	@Override
	public void create () {
        camera = new OrthographicCamera(SCREEN_W, SCREEN_H);
        viewport = new FitViewport(SCREEN_W, SCREEN_H, camera);
		sr = new ShapeRenderer(); 
		sr.setProjectionMatrix(camera.combined);
		sr.translate(-SCREEN_W / 2.0f, -SCREEN_H / 2.0f, 0);
		
        batch = new SpriteBatch();
		batch.setProjectionMatrix(camera.combined);
		
		map = new NoisedMap(new Vector2(SCREEN_W, SCREEN_H));//new Map(SCREEN_W, SCREEN_H, 17, 27, 5);
		marchingGrid = Utils.calculateMarchingGrid(map, map.getSize(), MARCHING_STEP);//(map.getMarchables(),  map, MARCHING_STEP);
		_noiseT = new Texture(map.getPixmap());
		
	}
	
	@Override
	public void render () {
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		batch.begin();
		batch.draw(_noiseT, -SCREEN_W/2, -SCREEN_H/2);
		batch.end();
		
		sr.begin(ShapeType.Filled);
		sr.setColor(Color.BLACK);
		//Utils.marchLine(map, marchingGrid, MARCHING_STEP, sr);
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
