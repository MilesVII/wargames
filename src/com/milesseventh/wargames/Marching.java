package com.milesseventh.wargames;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

public class Marching {
	public interface Marchable{
		public float getMeta(float x, float y);
		public float getMetaThreshold();
		//public float interpolate(double a, double b);
	}
	private class Line{
		public float x1, y1, x2, y2;
		public Line(float _x1, float _y1, float _x2, float _y2){
			x1 = _x1; y1 = _y1; x2 = _x2; y2 = _y2;
		}
	}
	public enum Mode{
		RAW, PRECALCULATED, PRERENDERED
	}
	
	private float[][] grid;
	private float step;
	private Marchable victim;
	private Vector2 size;
	private ArrayList<Line> calculated = new ArrayList<Line>();
	private Pixmap pm;
	private Mode mode;
	
	public Marching(Marchable _v, Vector2 _size, float _st, Mode _m){
		victim = _v;
		step = _st;
		size = _size;
		mode = _m;
		if (mode == Mode.PRERENDERED){
			pm = new Pixmap((int)size.x, (int)size.y, Pixmap.Format.RGBA8888);
			pm.setColor(Color.BLACK);
		}
		refresh();
	}
	
	public void refresh(){
		grid = calculateGrid(victim, size, step);
		if (mode != Mode.RAW){
			calculated.clear();
			run(null);
		}
	}
	
	public static float[][] calculateGrid(Marchable _meta, Vector2 _size, float _step){
		float[][] grid = new float[(int)(Math.floor(_size.x / _step))][(int)(Math.floor(_size.y / _step))];
		for (int _x = 0; _x < grid.length; _x++)
			for (int _y = 0; _y < grid[0].length; _y++)
				grid[_x][_y] = _meta.getMeta(_x * _step, _y * _step);//metaValue(_cs, _map, _x * _step, _y * _step);
		return grid;
	}
	
	private void run(ShapeRenderer _sr){
		Vector2 _from = new Vector2(0, 0), _to = new Vector2(0, 0);
		float a, b, c, d;
		for (int _x = 0; _x < grid.length - 1; _x++)
			for (int _y = 0; _y < grid[0].length - 1; _y++){
				a = grid[_x][_y];
				b = grid[_x + 1][_y];
				c = grid[_x + 1][_y + 1];
				d = grid[_x][_y + 1];
				float _mthr = victim.getMetaThreshold();
				short _case = (short) (((a >= _mthr)?8:0) +
									  ((b >= _mthr)?4:0) +
									  ((c >= _mthr)?2:0) +
									  ((d >= _mthr)?1:0));
				
				/*if (_case == 15)
					_sr.rect(_x * _step, _y * _step, _step, _step);*/
				if (_case > 7)
					_case = (short)(15 -_case);
				if (_case != 0){
					switch(_case){
					case(1):
						_from.x = 0; _from.y = interpolate(a, d, victim.getMetaThreshold());
						_to.x = interpolate(d, c, victim.getMetaThreshold()); _to.y = 1;
						break;
					case(2):
						_from.x = interpolate(d, c, victim.getMetaThreshold()); _from.y = 1;
						_to.x = 1; _to.y = interpolate(b, c, victim.getMetaThreshold());
						break;
					case(3):
						_from.x = 0; _from.y = interpolate(a, d, victim.getMetaThreshold());
						_to.x = 1; _to.y = interpolate(b, c, victim.getMetaThreshold());
						break;
					case(4):
						_from.x =  interpolate(a, b, victim.getMetaThreshold()); _from.y = 0;
						_to.x = 1; _to.y =  interpolate(b, c, victim.getMetaThreshold());
						break;
					case(6):
						_from.x =  interpolate(a, b, victim.getMetaThreshold()); _from.y = 0;
						_to.x =  interpolate(d, c, victim.getMetaThreshold()); _to.y = 1;
						break;
					case(7):
						_from.x = 0; _from.y =  interpolate(a, d, victim.getMetaThreshold());
						_to.x =  interpolate(a, b, victim.getMetaThreshold()); _to.y = 0;
						break;
					}
					switch (mode){
					case PRERENDERED:
						pm.drawLine((int)((_x + _from.x) * step), (int)(size.y - 1 - (_y + _from.y) * step),
									(int)((_x + _to.x) * step), (int)(size.y - 1 - (_y + _to.y) * step));
						break;
					case PRECALCULATED:
						calculated.add(new Line((_x + _from.x) * step, (_y + _from.y) * step,
								(_x + _to.x) * step, (_y + _to.y) * step));
						break;
					case RAW:
						_sr.line((_x + _from.x) * step, (_y + _from.y) * step,
								(_x + _to.x) * step, (_y + _to.y) * step);
					}
				}
			}
	}

	public void render(ShapeRenderer _sr){
		switch (mode){
		case RAW:
			run(_sr);
			break;
		case PRERENDERED:
			System.out.println("Use getRendered()");
			break;
		case PRECALCULATED:
			for (Line line: calculated)
				_sr.line(line.x1, line.y1,
						line.x2, line.y2);
		}
	}
	
	public Pixmap getRendered(){
		return pm;
	}
	
	private float interpolate(float _a, float _b, float _t){
		return (float)((_t - _a) / (_b - _a));
	}
}
