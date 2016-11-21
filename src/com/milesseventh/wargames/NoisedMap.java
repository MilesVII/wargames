package com.milesseventh.wargames;

import java.util.Random;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import net.jlibnoise.generator.Perlin;

public class NoisedMap implements Marching.Marchable{
	private float[][] noiseMap;
	private Vector2 size;
	private Pixmap pm;
	private Random r = new Random();
	
	public NoisedMap(Vector2 _size){
		size = _size;
		
		noiseMap = new float[getWidth()][getHeight()];
		pm = new Pixmap(getWidth(), getHeight(), Pixmap.Format.RGBA8888);
		float _noise, _dist;
		Perlin _noiseGen = new Perlin();
		_noiseGen.setSeed(r.nextInt());
		for (int x = 0; x < getWidth(); x++)
			for (int y = 0; y < getHeight(); y++){
					_noise = 0;
					_noise += (float) getNoise(_noiseGen, x, y, 4f);
					//_noise += (float) getNoise(_noiseGen, x, y, 8f);//Damn gauss distribution
					//_noise += (float) getNoise(_noiseGen, x, y, 16);
					_noise += (float) getNoise(_noiseGen, x, y, 32f);
					_noise += (float) getNoise(_noiseGen, x, y, 64f);
					//_noise += (float) getNoise(_noiseGen, x, y, 128f);
					//_noise += (float) getNoise(_noiseGen, x, y, 256f);
					_noise *= 2.7f;
					if (_noise > 1f)
						_noise = 1;
					if (_noise < 0f)
						_noise = 0;
					if (_noise < 0.5f)
						pm.setColor(Color.LIME);
						//pm.setColor(Utils.getGradColor(Color.LIME, Color.BROWN, _noise * 2));
					else
						pm.setColor(Color.WHITE);
						//pm.setColor(Utils.getGradColor(Color.BROWN, Color.WHITE, _noise * 2 - 1));
						
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
		float e = (float)((1 / _zoom) * 
				(_noise.getValue(x / (double) getWidth() * _zoom, 
								y / (double) getHeight() * _zoom, 
															0f) + 1f) / 2f);
		//System.out.println(e);
		return (float)Math.pow(e, 1f);
	}

	@Override
	public float getMeta(float _x, float _y){
		return noiseMap[(int)_x][(int)_y];
	}

	@Override
	public float getMetaThreshold(){
		return 0.5f;//getMeta returns [0;1]
	}
}
