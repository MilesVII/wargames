package com.milesseventh.wargames;

import java.util.Random;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Blending;
import com.badlogic.gdx.math.Vector2;

import net.jlibnoise.generator.Perlin;

public class NoisedMap implements Marching.Marchable, Pathfinder.Stridable{
	public static class ColorScheme{
		public Color WATER, GROUND, MOUNTAIN, TOP;
		public ColorScheme(Color _w, Color _g, Color _m, Color _t){
			WATER = _w;
			GROUND = _g;
			MOUNTAIN = _m;
			TOP = _t;
		}
	}
	
	private static Color workingColor = Color.BLACK;
	private float[][] noiseMap;
	private Vector2 size, center;
	private Pixmap pm;
	private Random r = new Random();
	private ColorScheme cs;
	
	public NoisedMap(Vector2 _size, ColorScheme _cs){
		size = _size;
		cs = _cs;
		center = size.cpy().scl(.5f);
		
		Color[] _c = {cs.GROUND, cs.MOUNTAIN, cs.TOP};
		noiseMap = new float[getWidth()][getHeight()];
		pm = new Pixmap(getWidth(), getHeight(), Pixmap.Format.RGBA8888);
		float _noise;
		Perlin _noiseGen = new Perlin();
		_noiseGen.setSeed(r.nextInt());
		for (int x = 0; x < getWidth(); x++)
			for (int y = 0; y < getHeight(); y++){
					_noise = 0;
					_noise += (float) getNoise(_noiseGen, x, y, 4f);
					//_noise += (float) getNoise(_noiseGen, x, y, 8f);
					//_noise += (float) getNoise(_noiseGen, x, y, 16);
					_noise += (float) getNoise(_noiseGen, x, y, 32f);
					_noise += (float) getNoise(_noiseGen, x, y, 64f);
					//_noise += (float) getNoise(_noiseGen, x, y, 128f);
					//_noise += (float) getNoise(_noiseGen, x, y, 256f);
					_noise *= 2.7f;//Damn gauss distribution
					_noise = bulge(x, y, _noise);
					if (_noise > 1f)
						_noise = 1;
					if (_noise < 0f)
						_noise = 0;
					pm.setColor(Utils.getGradColor(_c, _noise));
					noiseMap[x][getHeight() - y - 1] = _noise;//getHeight() - y - 1
					pm.drawPixel(x, y);
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
	
	private float getNoise(Perlin _noise, int x, int y, double _zoom){
		return (float)((1 / _zoom) * 
				(_noise.getValue(x / (double) getWidth() * _zoom, 
								y / (double) getHeight() * _zoom, 
															0f) + 1f) / 2f);
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
