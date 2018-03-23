package com.milesseventh.wargames;

import java.util.Random;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class HeightMap implements Marching.Marchable, Pathfinder.Stridable{
	public static final Color[] DEFAULT_SCHEME = {
			Color.RED,
			new Color(1, .6824f, .2706f, 1),
			new Color(1, .6824f, .2706f, 1), 
			Color.BROWN,
			Color.LIGHT_GRAY,
			Color.WHITE
		};
	
	//private static Color workingColor = Color.BLACK;
	private float[][] noiseMap;
	private Vector2 size, center;
	private Pixmap pm;
	private Random r = new Random();

	public HeightMap(Vector2 _size, Color[] cs){
		size = _size;
		center = size.cpy().scl(.5f);
		
		noiseMap = new float[getWidth()][getHeight()];
		pm = new Pixmap(getWidth(), getHeight(), Pixmap.Format.RGBA8888);
		float noiseValue;
		SimplexNoise noise = new SimplexNoise(r.nextInt());
		for (int x = 0; x < getWidth(); x++)
			for (int y = 0; y < getHeight(); y++){
				noiseValue = getNoise(noise, x, y, 16f) * 3;
				noiseValue += getNoise(noise, x, y, 32f);
				//noiseValue += getNoise(noise, x, y, 64f);
				noiseValue /= 4f;
				noiseValue = bulge(x, y, noiseValue);
				noiseValue = MathUtils.clamp(noiseValue, 0, 1);
				noiseValue = fade(noiseValue);
				noiseValue = MathUtils.clamp(noiseValue, 0, 1);
				
				pm.setColor(Utils.getGradColor(cs, noiseValue));
				noiseMap[x][getHeight() - y - 1] = noiseValue;//getHeight() - y - 1
				pm.drawPixel(x, y);
				WG.antistatic.updateLoadingBar((x * getHeight() + y) / (float)(getWidth() * getHeight()));
			}
	}

	public int getWidth(){
		return (int)size.x;
	}

	public int getHeight(){
		return (int)size.y;
	}
	
	public Vector2 getSize(){
		return size;
	}
	
	public Pixmap getPixmap(){
		return pm;
	}
	
	private float getNoise(SimplexNoise noise, int x, int y, double zoom){
		return (float)((noise.eval(x / size.x * zoom, y / size.y * zoom) + 1) / 2f);
	}
	
	private float fade(float t){
		return t * t * t * (t * (t * 6 - 15) + 10); 
	}
	
	private float bulge(int x, int y, float _n){
		return (_n + center.dst(x, y) / center.len()) / 1.6f;
	}
	
	@Override
	public float getMeta(float _x, float _y){
		return noiseMap[(int)_x][(int)_y];
	}

	@Override
	public float getMetaThreshold(){
		return 0.5f;//getMeta returns [0;1]
	}

	@Override
	public boolean isWalkable(float _x, float _y) {
		return getMeta(_x, _y) < getMetaThreshold();
	}
}
