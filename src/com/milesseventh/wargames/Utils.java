package com.milesseventh.wargames;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;

public class Utils {
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

	public static void marchLine(float[][] _grid, float _step, ShapeRenderer _sr){
		Vector2 _from = new Vector2(0, 0), _to = new Vector2(0, 0);
		double a, b, c, d;
		for (int _x = 0; _x < _grid.length - 1; _x++)
			for (int _y = 0; _y < _grid[0].length - 1; _y++){
				short _case = (short) (((_grid[_x][_y] >= 1)?8:0) +
									  ((_grid[_x + 1][_y] >= 1)?4:0) +
									  ((_grid[_x + 1][_y + 1] >= 1)?2:0) +
									  ((_grid[_x][_y + 1] >= 1)?1:0));
				if (_case > 7)
					_case = (short)(15 -_case);
				if (!(_case == 0 || _case == 5)){
					a = _grid[_x][_y];
					b = _grid[_x + 1][_y];
					c = _grid[_x + 1][_y + 1];
					d = _grid[_x][_y + 1];
					
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
					_sr.line((_x + _from.x) * _step, (_y + _from.y) * _step,
							(_x + _to.x) * _step, (_y + _to.y) * _step);
				}
			}
		
	}
	
	public static void marchLine(Marchable[] _cs, Map _map, float _step, ShapeRenderer _sr){
		marchLine(calculateMarchingGrid(_cs, _map, _step), _step, _sr);
	}
	
	public static float[][] calculateMarchingGrid(Marchable[] _cs, Map _map, float _step){
		float[][] grid = new float[(int)(Math.floor(_map.getWidth() / _step))][(int)(Math.floor(_map.getHeight() / _step))];
		for (int _x = 0; _x < grid.length; _x++)
			for (int _y = 0; _y < grid[0].length; _y++)
				grid[_x][_y] = metaF(_cs, _map, _x * _step, _y * _step);
		return grid;
	}
	
	private static float metaF(Marchable[] _cs, Map _map, float _x, float _y){
		float _sum = 0;
		for (Marchable march: _cs){
			Circle c = march.getCircle();
			_sum += Math.pow(c.radius /  Vector2.dst(_x, _y, c.x, c.y), 5);//Math.floor(c.radius / (double)Vector2.dst((float)_x, (float)_y, c.x, c.y));
		}
		return _sum;
	}
	
	private static float interpolate(double _a, double _b){
		return (float)((1 - _a) / (_b - _a));
	}
}
