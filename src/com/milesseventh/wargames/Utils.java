package com.milesseventh.wargames;

import java.util.ArrayList;
import java.util.Random;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;;

public class Utils {
	public static Vector2 WorldMousePosition = new Vector2(), UIMousePosition = new Vector2();//Updated via WG.java, update();
	public static boolean isTouchJustReleased = false;
	public static final int NULL_ID = -1;
	//public static final Heartstrings heartstrings = new Heartstrings();
	
	public static float projectX(float _len, float _dir){
		return (float)(_len * Math.cos(Math.toRadians(_dir)));
	}
	
	public static float projectY(float _len, float _dir){
		return (float)(_len * Math.sin(Math.toRadians(_dir)));
	}

	public static boolean isBuildingAllowed(HeightMap _map, float _x, float _y){
		return _map.getMeta(_x, _y) < _map.getMetaThreshold();
	}
	
	private static Color cholder = new Color();
	public static Color getGradColor(Color _from, Color _to, float _percent){
		return cholder.set(MathUtils.lerp(_from.r, _to.r, _percent),
		                   MathUtils.lerp(_from.g, _to.g, _percent),
		                   MathUtils.lerp(_from.b, _to.b, _percent), 1);
	}
	public static Color getRoundGradColor(Color _from, Color _to, float _percent){
		return getGradColor(_from, _to, Math.round(_percent));
	}
	
	public static Color getGradColor(Color[] colors, float percent){
		float step = 1 / (float) (colors.length - 1);
		if (percent == 1)
			return colors[colors.length - 1];
		int c = Math.round((percent - percent % step) / step);
		return getRoundGradColor(colors[c], colors[c + 1], (percent % step) / step);
	}
	
	private static final float DBG_MIN_CITY_H = .10f, DBG_MAX_CITY_H = .32f;
	public static Vector2 debugFindAPlaceForStructure(HeightMap _map){
		Vector2 _place;
		Random _r = new Random();
		do {
			_place = new Vector2(_r.nextInt(_map.getWidth()), _r.nextInt(_map.getHeight()));
		} while(_map.getMeta(_place.x, _place.y) > DBG_MAX_CITY_H || _map.getMeta(_place.x, _place.y) < DBG_MIN_CITY_H);
		return _place;
	}
	
	private static final int DBG_MIN_CITY_DST = 17, DBG_MAX_CITY_DST = 48;
	public static boolean debugCheckPlaceForNewStructure(HeightMap _map, Faction _f, Vector2 _place){
		Structure _nrst = debugFindNearestStructure(_f.structs, _place);
		return (_nrst.getPosition().dst2(_place) < DBG_MAX_CITY_DST * DBG_MAX_CITY_DST &&
			_nrst.getPosition().dst2(_place) > DBG_MIN_CITY_DST * DBG_MIN_CITY_DST &&
			(_map.getMeta(_place.x, _place.y) < DBG_MAX_CITY_H && _map.getMeta(_place.x, _place.y) > DBG_MIN_CITY_H));
	}
	
	private static Structure debugFindNearestStructure(ArrayList<Structure> cities, Vector2 _from){
		Structure minStructure = cities.get(0);
		for (Structure _to: cities)
			if (_from.dst2(_to.getPosition()) < _from.dst2(minStructure.getPosition()))
				minStructure = _to;
		return minStructure;
	}
	
	public static float getAngle(Vector2 point){
		//return point.angle();
		return (float) Math.toDegrees((Math.atan2(point.y, point.x) > 0 ? Math.atan2(point.y, point.x) : Math.atan2(point.y, point.x) + Math.PI * 2));
	}
	
	public static void drawTrueArc(ShapeRenderer sr, Vector2 cnt, float radius, float start, float length, int segments){
		for (int i = 0; i < segments; ++i){
			double angle0 = Math.toRadians(start) + Math.toRadians(length) * ((i + 0) / (double)segments);
			double angle1 = Math.toRadians(start) + Math.toRadians(length) * ((i + 1) / (double)segments);
			sr.rectLine((float) Math.cos(angle0) * radius + cnt.x, (float) Math.sin(angle0) * radius + cnt.y, 
			            (float) Math.cos(angle1) * radius + cnt.x, (float) Math.sin(angle1) * radius + cnt.y, 3);
		}
	}
	
	public static String splitIntoLines(String victim, int line){
		if (victim.length() <= line)
			return victim;
		String[] words = victim.split(" ");
		int counter = 0;
		StringBuilder sb = new StringBuilder();
		/*for (int i = 0; i < words.length; ++i){
			if (counter + words[i].length() > line){
				if (counter != 0)
					sb.append('\b');
				sb.append('\n');
				counter = 0;             wrong
			}
			sb.append(words[i]);
			sb.append(' ');
			counter += words[i].length() + 1;
		}*/
		for (String word : words){
			sb.append(word);
			sb.append(' ');
			counter += word.length() + 1;
			if (counter > line){
				sb.append('\b');
				sb.append('\n');
				counter = 0;
			}
		}
		return sb.toString();
	}
	
	public static <T> boolean arrayContains(T[] a, T b){
		for (T x : a)
			if (x.equals(b))
				return true;
		return false;
	}
	
	private static final int VECTORS_IN_POOL = 128;
	public static Vector2[] vpool = new Vector2[VECTORS_IN_POOL];
	private static int vectorsCounter = 0, holder;
	public static Vector2 getVector(){//new Vector2() alternative
		return getVector(0, 0);
	}
	public static Vector2 getVector(Vector2 in){//cpy() alternative
		return getVector(in.x, in.y);
	}
	public static Vector2 getVector(float x, float y){//new Vector2(x, y) alternative
		if (vpool[vectorsCounter] == null)
			vpool[vectorsCounter] = new Vector2();
		vpool[vectorsCounter].x= x;
		vpool[vectorsCounter].y= y;
		holder = vectorsCounter;
		++vectorsCounter;
		vectorsCounter %= VECTORS_IN_POOL;
		return vpool[holder];
	}
	private static Color color = new Color();
	public static Color getColor(int r, int g, int b, int a){
		color.set(r / 255f, g / 255f, b / 255f, a / 255f);
		return color;
	}
}
