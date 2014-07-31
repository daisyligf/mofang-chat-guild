package com.mofang.chat.guild.component;

import org.json.JSONObject;

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
public class GiftComponent
{
	public static int getGuildGiftCount(long guildId) throws Exception
	{
		///先从本地获取公会礼包总数, 如果本地没有缓存，则调用服务端接口获取
		String key = RedisKey.CACHE_GUILD_GIFT_COUNT_KEY_PREFIX + guildId;
		ResultCacheRedis cacheRedis = ResultCacheRedisImpl.getInstance();
		String value = cacheRedis.getCache(key);
		if(!StringUtil.isNullOrEmpty(value))
			return Integer.parseInt(value);
		
		/// 调用服务端接口获取
		int giftCount = getGuildGiftCountByAPI(guildId);
		
		try
		{
			///保存到redis中
			cacheRedis.saveCache(key, String.valueOf(giftCount), GlobalConfig.GUILD_GIFT_COUNT_EXPIRE);
			return giftCount;
		}
		catch(Exception e)
		{	
			throw e;
		}
	}
	
	public static int getGuildGameGiftCount(long guildId, int gameId) throws Exception
	{
		///先从本地获取公会游戏礼包总数, 如果本地没有缓存，则调用服务端接口获取
		String key = RedisKey.CACHE_GUILD_GAME_GIFT_COUNT_KEY_PREFIX + guildId + "_" + gameId;
		ResultCacheRedis cacheRedis = ResultCacheRedisImpl.getInstance();
		String value = cacheRedis.getCache(key);
		if(!StringUtil.isNullOrEmpty(value))
			return Integer.parseInt(value);
		
		/// 调用服务端接口获取
		int giftCount = getGuildGameGiftCountByAPI(guildId, gameId);
		
		try
		{
			///保存到redis中
			cacheRedis.saveCache(key, String.valueOf(giftCount), GlobalConfig.GUILD_GAME_GIFT_COUNT_EXPIRE);
			return giftCount;
		}
		catch(Exception e)
		{	
			throw e;
		}
	}
	
	private static int getGuildGiftCountByAPI(long guildId)
	{
		String url = GlobalConfig.GUILD_GIFT_COUNT_URL + "?guild_id=" + guildId;
		try
		{
			String result = HttpClientSender.get(GlobalObject.HTTP_CLIENT_API, url);
			JSONObject json = new JSONObject(result);
			int code = json.optInt("code", -1);
			if(0 != code)
				return 0;
			
			JSONObject data = json.optJSONObject("data");
			if(null == data)
				return 0;
			
			return data.optInt("total", 0);
		}
		catch(Exception e)
		{
			return 0;
		}
	}
	
	private static int getGuildGameGiftCountByAPI(long guildId, int gameId)
	{
		String url = GlobalConfig.GUILD_GAME_GIFT_COUNT_URL + "?guild_id=" + guildId + "&game_id=" + gameId;
		try
		{
			String result = HttpClientSender.get(GlobalObject.HTTP_CLIENT_API, url);
			JSONObject json = new JSONObject(result);
			int code = json.optInt("code", -1);
			if(0 != code)
				return 0;
			
			JSONObject data = json.optJSONObject("data");
			if(null == data)
				return 0;
			
			return data.optInt("total", 0);
		}
		catch(Exception e)
		{
			return 0;
		}
	}
}