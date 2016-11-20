package com.milesseventh.wargames;

import java.util.ArrayList;
import java.util.Random;

import com.badlogic.gdx.math.Vector2;

public class CircledMap {
	private static Random r = new Random();
	private ArrayList<Mountain> mountains = new ArrayList<Mountain>();
	private float width = 0, height = 0;
	private final float MOUNTAIN_RAD_SHAKE = 10;//Must be fixed
	private final float GEN_GRAPH_LEN = 7, GEN_GRAPH_ANGLESHAKE = 90;//Degrees
	
	public CircledMap(float _w, float _h, int _mountainsMinSize, int _mountains, int _chains){
		width = _w;
		height = _h;
		Vector2 _center = new Vector2(_w / 2f, _h / 2f);

		Vector2 _pos;
		float _angleshake;
		
		for (int n = 0; n < _chains; n++){
			_pos = new Vector2(r.nextFloat() * _w, r.nextFloat() * _h);
			_angleshake = Utils.getAngle(_pos, _center);
	
			for (int m = 0; m < _mountains; m++){
				mountains.add(new Mountain(_pos, _mountainsMinSize, _mountainsMinSize + MOUNTAIN_RAD_SHAKE * r.nextFloat()));
				_angleshake += GEN_GRAPH_ANGLESHAKE * r.nextFloat() - GEN_GRAPH_ANGLESHAKE / 2f;
				_pos.set(_pos.x + Utils.projectX(GEN_GRAPH_LEN, _angleshake), _pos.y + Utils.projectY(GEN_GRAPH_LEN, _angleshake));
			}
		}
	}
	
	public float getWidth(){
		return width;
	}
	
	public float getHeight(){
		return height;
	}
	
	public ArrayList<Mountain> getMountains(){
		return mountains;
	}
	
	/*public Marchable[] getMarchables(){
		return getMountains().toArray(new Marchable[getMountains().size()]);
	}*/
}
