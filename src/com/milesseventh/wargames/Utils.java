package com.milesseventh.wargames;

import java.util.ArrayList;
import java.util.Random;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.milesseventh.wargames.units.City;;

public class Utils {
	public static Vector2 WorldMousePosition = new Vector2(), HUDMousePosition = new Vector2();//Updated via WG.java, update();
	public static boolean isTouchJustReleased = false;
	
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

	/* Moved to Marching.java
	public static float[][] calculateMarchingGrid(Marchable _meta, Vector2 _size, float _step){
		float[][] grid = new float[(int)(Math.floor(_size.x / _step))][(int)(Math.floor(_size.y / _step))];
		for (int _x = 0; _x < grid.length; _x++)
			for (int _y = 0; _y < grid[0].length; _y++)
				grid[_x][_y] = _meta.getMeta(_x * _step, _y * _step);//metaValue(_cs, _map, _x * _step, _y * _step);
		return grid;
	}*/
	
	public static boolean isBuildingAllowed(HeightMap _map, float _x, float _y){
		/*float min = WG.INFINITY, _dist;
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
			return WG.INFINITY;*/
		return _map.getMeta(_x, _y) < _map.getMetaThreshold();
	}

	public static Color getGradColor(Color _from, Color _to, float _percent){
		return new Color(MathUtils.lerp(_from.r, _to.r, _percent),
						MathUtils.lerp(_from.g, _to.g, _percent),
						MathUtils.lerp(_from.b, _to.b, _percent), 1);
	}
	
	public static Color getGradColor(Color[] _colors, float _percent){
		int _intervals =  _colors.length - 1;
		for (int c = 0; c < _intervals; c++)
			if (_percent <= (1 / (float)_intervals * (float)(c + 1f)))
				return new Color(getGradColor(_colors[c], _colors[c + 1], _percent * _intervals - 1 / (float)_intervals * (float)c));
		return Color.BLACK;
	}
	
	private static final float DBG_MIN_CITY_H = .10f, DBG_MAX_CITY_H = .32f;
	public static Vector2 debugFindAPlaceForCity(HeightMap _map){
		Vector2 _place;
		Random _r = new Random();
		do {
			_place = new Vector2(_r.nextInt(_map.getWidth()), _r.nextInt(_map.getHeight()));
		} while(_map.getMeta(_place.x, _place.y) > DBG_MAX_CITY_H || _map.getMeta(_place.x, _place.y) < DBG_MIN_CITY_H);
		return _place;
	}
	
	private static final int DBG_MIN_CITY_DST = 17, DBG_MAX_CITY_DST = 48;
	public static boolean debugCheckPlaceForNewCity(HeightMap _map, Fraction _f, Vector2 _place){
		City _nrst = debugFindNearestCity(_f.getCities(), _place);

		return (_nrst.getPosition().dst2(_place) < DBG_MAX_CITY_DST * DBG_MAX_CITY_DST &&
			_nrst.getPosition().dst2(_place) > DBG_MIN_CITY_DST * DBG_MIN_CITY_DST &&
			(_map.getMeta(_place.x, _place.y) < DBG_MAX_CITY_H && _map.getMeta(_place.x, _place.y) > DBG_MIN_CITY_H));
	}
	
	private static City debugFindNearestCity(ArrayList<City> cities, Vector2 _from){
		City minCity = cities.get(0);
		for (City _to: cities)
			if (_from.dst2(_to.getPosition()) < _from.dst2(minCity.getPosition()))
				minCity = _to;
		return minCity;
	}
	
	public static float getAngle(Vector2 point){
		return (float) Math.toDegrees((Math.atan2(point.y, point.x) > 0 ? Math.atan2(point.y, point.x) : Math.atan2(point.y, point.x) + Math.PI * 2));
	}
	
	public static void drawTrueArc(ShapeRenderer sr, Vector2 cnt, float radius, float start, float length, int segments){
		for (int i = 0; i < segments; ++i){
			double angle0 = Math.toRadians(start) + Math.toRadians(length) * ((i + 0) / (double)segments);
			double angle1 = Math.toRadians(start) + Math.toRadians(length) * ((i + 1) / (double)segments);
			sr.rectLine((float) Math.cos(angle0) * radius + cnt.x, (float) Math.sin(angle0) * radius + cnt.y, 
			            (float) Math.cos(angle1) * radius + cnt.x, (float) Math.sin(angle1) * radius + cnt.y, 2);
		}
	}
	
}
