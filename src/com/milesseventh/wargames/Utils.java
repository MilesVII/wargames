package com.milesseventh.wargames;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;

public class Utils {
	public interface Marchable{
		public float getMeta(float x, float y);
		public float getMetaThreshold();
	}
	
	public static float projectX(float _len, float _dir){
		return (float)(_len * Math.cos(Math.toRadians(_dir)));
	}
	
	public static float projectY(float _len, float _dir){
		return (float)(_len * Math.sin(Math.toRadians(_dir)));
	}
	
	//Wrong
	public static float getAngle(Vector2 _from, Vector2 _to){
		float len =_from.dst(_to);
		if (len == 0)
			return 0;
		float projectedX = Math.abs(_to.x - _from.x);
		float angle = (float) Math.toDegrees(Math.asin(projectedX / len));
		if (_to.x - _from.x > 0)
			if (_to.y - _from.y > 0)
				//I
				return angle;
			else
				//IV
				return 360 - angle;
		else
			if (_to.y - _from.y > 0)
				//II
				return 180 - angle;
			else
				//III
				return 270 + angle;
	}

	public static void marchLine(Marchable _meta, float[][] _grid, float _step, ShapeRenderer _sr){
		Vector2 _from = new Vector2(0, 0), _to = new Vector2(0, 0);
		double a, b, c, d;
		for (int _x = 0; _x < _grid.length - 1; _x++)
			for (int _y = 0; _y < _grid[0].length - 1; _y++){
				a = _grid[_x][_y];
				b = _grid[_x + 1][_y];
				c = _grid[_x + 1][_y + 1];
				d = _grid[_x][_y + 1];
				float _mthr = _meta.getMetaThreshold();
				short _case = (short) (((a >= _mthr)?8:0) +
									  ((b >= _mthr)?4:0) +
									  ((c >= _mthr)?2:0) +
									  ((d >= _mthr)?1:0));
				
				/*if (_case == 15)
					_sr.rect(_x * _step, _y * _step, _step, _step);*/
				if (_case > 7)
					_case = (short)(15 -_case);
				if (_case != 0 && _case != 5){
					switch(_case){
					case(1):
						_from.x = 0; _from.y = interpolate(a, d);
						_to.x = interpolate(d, c); _to.y = 1;
						break;
					case(2):
						_from.x = interpolate(d, c); _from.y = 1;
						_to.x = 1; _to.y = interpolate(b, c);
						break;
					case(3):
						_from.x = 0; _from.y = interpolate(a, d);
						_to.x = 1; _to.y = interpolate(b, c);
						break;
					case(4):
						_from.x =  interpolate(a, b); _from.y = 0;
						_to.x = 1; _to.y =  interpolate(b, c);
						break;
					case(6):
						_from.x =  interpolate(a, b); _from.y = 0;
						_to.x =  interpolate(d, c); _to.y = 1;
						break;
					case(7):
						_from.x = 0; _from.y =  interpolate(a, d);
						_to.x =  interpolate(a, b); _to.y = 0;
						break;
					}
					_sr.rectLine((_x + _from.x) * _step, (_y + _from.y) * _step,
							(_x + _to.x) * _step, (_y + _to.y) * _step, 2);
				}
			}
		
	}
	
	//public static void marchLine(Marchable[] _cs, Map _map, float _step, ShapeRenderer _sr){
	//	marchLine(calculateMarchingGrid(_cs, _map, _step), _step, _sr);
	//}
	
	public static float[][] calculateMarchingGrid(Marchable _meta, Vector2 _size, float _step){
		float[][] grid = new float[(int)(Math.floor(_size.x / _step))][(int)(Math.floor(_size.y / _step))];
		for (int _x = 0; _x < grid.length; _x++)
			for (int _y = 0; _y < grid[0].length; _y++)
				grid[_x][_y] = _meta.getMeta(_x * _step, _y * _step);//metaValue(_cs, _map, _x * _step, _y * _step);
		return grid;
	}

	/*Move to Unit.java
	public static float metaValue(Marchable[] _cs, Map _map, float _x, float _y){
		float _sum = 0;
		for (Marchable march: _cs){
			Circle c = march.getCircle();
			_sum += Math.pow(c.radius /  Vector2.dst(_x, _y, c.x, c.y), 5);//Math.floor(c.radius / (double)Vector2.dst((float)_x, (float)_y, c.x, c.y));
		}
		return _sum;
	}*/
	
	
	/*Transform into collisionCheck
	public static float minDistanceToMarchable(Marchable[] _cs, Map _map, float _x, float _y){
		float min = WG.INFINITY, _dist;
		Marchable _ = null;
		for (Marchable march: _cs){
			_dist = Vector2.dst(march.getCircle().x, march.getCircle().y, _x, _y);
			if (_dist <= march.getCircle().radius && _dist < min){
				_ = march;//.getCircle().radius
				min = _dist;
			}
		}
		if (min != WG.INFINITY)
			return (min / _.getCircle().radius);
		else
			return WG.INFINITY;
	}*/
	
	public static Color getGradColor(Color _from, Color _to, float _percent){
		return new Color(_from.r + (_to.r - _from.r) * _percent, 
						 _from.g + (_to.g - _from.g) * _percent, 
						 _from.b + (_to.b - _from.b) * _percent, 1);
	}
	
	private static float interpolate(double _a, double _b){
		return (float)((1 - _a) / (_b - _a));
	}
}
