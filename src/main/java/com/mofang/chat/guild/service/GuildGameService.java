package com.mofang.chat.guild.service;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 
 * @author zhaodx
 *
 */
public interface GuildGameService
{
	public void edit(long guildId, String guildName, long userId, JSONArray addGames, JSONArray delGames) throws Exception;
	
	public JSONObject getGuildList(int gameId, int start, int end) throws Exception;
	
	public JSONObject getHotGuildList(int gameId) throws Exception;
}