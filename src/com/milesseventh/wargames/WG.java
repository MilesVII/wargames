package com.milesseventh.wargames;

import java.util.ArrayList;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class WG extends ApplicationAdapter {
	public final int FRACTION_GOD = 0,
					 FRACTION_SEVENTH = 1,
					 FRACTION_ID_OFFSET = 2;
    public static final int SCREEN_W = 200;
    public static final int SCREEN_H = 200;
    public static final int MARCHING_STEP = 1;
	
	SpriteBatch batch;
	Map map;
    private Camera camera;
	private Viewport viewport;
    private ShapeRenderer sr; 
    private float[][] marchingGrid;
	
	@Override
	public void create () {
		
        camera = new OrthographicCamera(SCREEN_W, SCREEN_H);
        viewport = new FitViewport(SCREEN_W, SCREEN_H, camera);
		sr = new ShapeRenderer(); 
		sr.setProjectionMatrix(camera.combined);
		sr.translate(-SCREEN_W / 2.0f, -SCREEN_H / 2.0f, 0);
		
        batch = new SpriteBatch();
		batch.setProjectionMatrix(camera.combined);
		
		map = new Map(SCREEN_W, SCREEN_H, 7, 7, 7);
		marchingGrid = Utils.calculateMarchingGrid(map.getMountains().toArray(new Marchable[map.getMountains().size()]),  map, MARCHING_STEP);
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		sr.setColor(Color.GRAY);
		sr.begin(ShapeType.Filled);
		Circle _c;
		for (Mountain _devildriver: map.getMountains()){
			_c = _devildriver.getCircle();
			sr.circle(_c.x, _c.y, _c.radius);
		}
		sr.end();
		sr.setColor(Color.BLACK);
		sr.begin(ShapeType.Line);
		Utils.marchLine(marchingGrid, MARCHING_STEP, sr);
		sr.end();
		System.out.println(Gdx.graphics.getFramesPerSecond());
		/*batch.begin();
		batch.draw(img, 0, 0);
		batch.end();*/
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
