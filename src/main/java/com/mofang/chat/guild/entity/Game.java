package com.mofang.chat.guild.entity;

import org.json.JSONObject;

public class Game
{
	private int gameId;
	private String gameName;
	private String icon;
	
	public int getGameId()
	{
		return gameId;
	}
	
	public void setGameId(int gameId)
	{
		this.gameId = gameId;
	}
	
	public String getGameName()
	{
		return gameName;
	}
	
	public void setGameName(String gameName) 
	{
		this.gameName = gameName;
	}
	
	public String getIcon() 
	{
		return icon;
	}
	
	public void setIcon(String icon)
	{
		this.icon = icon;
	}
	
	public JSONObject toJson()
	{
		JSONObject json = new JSONObject();
		try
		{
			json.put("id", gameId);
			json.put("name", gameName == null ? "" : gameName);
			json.put("icon", icon == null ? "" : icon);
			return json;
		}
		catch(Exception e)
		{
			return null;
		}
	}
	
	public static Game buildByJson(JSONObject json)
	{
		Game model = new Game();
		try
		{
			model.setGameId(json.optInt("id", 0));
			model.setGameName(json.optString("name", ""));
			model.setIcon(json.optString("icon", ""));
			return model;
		}
		catch(Exception e)
		{
			return null;
		}
	}
}