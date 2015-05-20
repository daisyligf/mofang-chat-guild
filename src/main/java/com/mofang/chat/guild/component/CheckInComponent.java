package com.mofang.chat.guild.component;

import java.util.HashMap;
import java.util.Map;

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
	public static Map<String, Integer> getGuildCheckinNum(long guildId) throws Exception
	{
		///先从本地获取用户信息, 如果本地没有缓存，则调用服务端接口获取
		String key = RedisKey.CACHE_GUILD_CHECKIN_NUM_KEY_PREFIX + guildId;
		String totalKey = RedisKey.CACHE_GUILD_CHECKIN_TOTAL_NUM_KEY_PREFIX + guildId;
		ResultCacheRedis cacheRedis = ResultCacheRedisImpl.getInstance();
		String value = cacheRedis.getCache(key);
		String totalValue = cacheRedis.getCache(totalKey);
		if(StringUtil.isInteger(value) && StringUtil.isInteger(totalValue))
		{
			Map<String, Integer> checkinMap = new HashMap<String, Integer>();
			checkinMap.put("num", Integer.parseInt(value));
			checkinMap.put("totalNum", Integer.parseInt(totalValue));
		}
			
		/// 调用服务端接口获取
		Map<String, Integer> checkinMap = getCheckinNumByAPI(guildId, null);
		try
		{
			///保存到redis中
			int num = checkinMap.get("num");
			int totalNum = checkinMap.get("totalNum");
			cacheRedis.saveCache(key, String.valueOf(num), GlobalConfig.GUILD_CHECKIN_NUM_EXPIRE);
			cacheRedis.saveCache(totalKey, String.valueOf(totalNum), GlobalConfig.GUILD_CHECKIN_NUM_EXPIRE);
			return checkinMap;
		}
		catch(Exception e)
		{	
			throw e;
		}
	}
	
	public static Map<String, Integer> getGuildCheckinNum(long guildId, String date) throws Exception
	{
		/// 调用服务端接口获取
		return getCheckinNumByAPI(guildId, date);
	}
	
	private static Map<String, Integer> getCheckinNumByAPI(long guildId, String date)
	{
		String url = GlobalConfig.GUILD_CHECKIN_NUM_URL;
		Map<String, Integer> checkinMap = new HashMap<String, Integer>();
		checkinMap.put("num", 0);
		checkinMap.put("totalNum", 0);
		try
		{
			String param = "?id_alias=guild&signid=" + guildId;
			if(!StringUtil.isNullOrEmpty(date))
				param += "&date=" + date;
			String result = HttpClientSender.get(GlobalObject.HTTP_CLIENT_API, url + param);
			JSONObject json = new JSONObject(result);
			int code = json.optInt("code", -1);
			
			if(0 != code) {
				return checkinMap;
			}
				
			
			JSONObject data = json.optJSONObject("data");
			if(null == data) {
				return checkinMap;
			}
			checkinMap.put("num", data.optInt("num", 0));
			checkinMap.put("totalNum", data.optInt("totalNum", 0));
			return checkinMap;
		}
		catch(Exception e)
		{
			return checkinMap;
		}
	}
}