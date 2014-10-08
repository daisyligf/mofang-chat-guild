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
public class CheckInComponent
{	
	public static int getGuildCheckinNum(long guildId) throws Exception
	{
		///先从本地获取用户信息, 如果本地没有缓存，则调用服务端接口获取
		String key = RedisKey.CACHE_GUILD_CHECKIN_NUM_KEY_PREFIX + guildId;
		ResultCacheRedis cacheRedis = ResultCacheRedisImpl.getInstance();
		String value = cacheRedis.getCache(key);
		if(StringUtil.isInteger(value))
			return Integer.parseInt(value);
		
		/// 调用服务端接口获取
		int num = getCheckinNumByAPI(guildId, null);
		try
		{
			///保存到redis中
			cacheRedis.saveCache(key, String.valueOf(num), GlobalConfig.GUILD_CHECKIN_NUM_EXPIRE);
			return num;
		}
		catch(Exception e)
		{	
			throw e;
		}
	}
	
	public static int getGuildCheckinNum(long guildId, String date) throws Exception
	{
		/// 调用服务端接口获取
		return getCheckinNumByAPI(guildId, date);
	}
	
	private static int getCheckinNumByAPI(long guildId, String date)
	{
		String url = GlobalConfig.GUILD_CHECKIN_NUM_URL;
		try
		{
			String param = "?id_alias=guild&signid=" + guildId;
			if(!StringUtil.isNullOrEmpty(date))
				param += "&date=" + date;
			String result = HttpClientSender.get(GlobalObject.HTTP_CLIENT_API, url + param);
			JSONObject json = new JSONObject(result);
			int code = json.optInt("code", -1);
			if(0 != code)
				return 0;
			
			JSONObject data = json.optJSONObject("data");
			if(null == data)
				return 0;
			
			return data.optInt("num", 0);
		}
		catch(Exception e)
		{
			return 0;
		}
	}
}