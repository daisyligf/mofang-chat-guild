package com.mofang.chat.guild.component;

import org.json.JSONObject;

import com.mofang.chat.guild.entity.Game;
import com.mofang.chat.guild.global.GlobalConfig;
import com.mofang.chat.guild.global.GlobalObject;
import com.mofang.chat.guild.global.RedisKey;
import com.mofang.chat.guild.redis.ResultCacheRedis;
import com.mofang.chat.guild.redis.impl.ResultCacheRedisImpl;
import com.mofang.framework.net.http.HttpClientSender;
import com.mofang.framework.util.StringUtil;

/**
 * 
 * @author zhaodx
 *
 */
public class GameComponent
{
	public static Game getInfo(int gameId) throws Exception
	{
		///先从本地获取游戏信息, 如果本地没有缓存，则调用服务端接口获取
		String key = RedisKey.CACHE_GAME_INFO_KEY_PREFIX + gameId;
		ResultCacheRedis cacheRedis = ResultCacheRedisImpl.getInstance();
		String value = cacheRedis.getCache(key);
		if(!StringUtil.isNullOrEmpty(value))
		{
			JSONObject json = new JSONObject(value);
			return Game.buildByJson(json);
		}
		
		/// 调用服务端接口获取
		long start = System.currentTimeMillis();
		Game game = getInfoByAPI(gameId);
		long end = System.currentTimeMillis();
		GlobalObject.INFO_LOG.info("GAME_INFO_URL costs time " + (end - start) + " ms");
		if(null == game)
			return null;
		
		try
		{
			///保存到redis中
			cacheRedis.saveCache(key, game.toJson().toString(), GlobalConfig.GAME_INFO_EXPIRE);
			return game;
		}
		catch(Exception e)
		{	
			throw e;
		}
	}
	
	private static Game getInfoByAPI(int gameId)
	{
		String url = GlobalConfig.GAME_INFO_URL + "?id=" + gameId;
		try
		{
			String result = HttpClientSender.get(GlobalObject.HTTP_CLIENT_API, url);
			JSONObject json = new JSONObject(result);
			int code = json.optInt("code", -1);
			if(0 != code)
				return null;
			
			JSONObject data = json.optJSONObject("data");
			if(null == data)
				return null;
			
			Game game = new Game();
			game.setGameId(gameId);
			game.setGameName(data.optString("name", ""));
			game.setIcon(data.optString("app_icon", ""));
			return game;
		}
		catch(Exception e)
		{
		    	GlobalObject.ERROR_LOG.error("GameComponent.getInfoByAPI throws an error.", e);
			return null;
		}
	}
}