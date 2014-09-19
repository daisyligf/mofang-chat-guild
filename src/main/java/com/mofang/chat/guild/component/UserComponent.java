package com.mofang.chat.guild.component;

import org.json.JSONObject;

import com.mofang.chat.guild.entity.User;
import com.mofang.chat.guild.global.GlobalConfig;
import com.mofang.chat.guild.global.GlobalObject;
import com.mofang.chat.guild.global.RedisKey;
import com.mofang.chat.guild.global.common.UserStatus;
import com.mofang.chat.guild.global.common.UserType;
import com.mofang.chat.guild.redis.ResultCacheRedis;
import com.mofang.chat.guild.redis.impl.ResultCacheRedisImpl;
import com.mofang.framework.net.http.HttpClientSender;
import com.mofang.framework.util.StringUtil;

/**
 * 
 * @author zhaodx
 *
 */
public class UserComponent
{
	public static User getInfo(long userId) throws Exception
	{
		///先从本地获取用户信息, 如果本地没有缓存，则调用服务端接口获取
		String key = RedisKey.CACHE_USER_INFO_KEY_PREFIX + userId;
		ResultCacheRedis cacheRedis = ResultCacheRedisImpl.getInstance();
		String value = cacheRedis.getCache(key);
		if(!StringUtil.isNullOrEmpty(value))
		{
			JSONObject json = new JSONObject(value);
			return User.buildByJson(json);
		}
		
		/// 调用服务端接口获取
		long start = System.currentTimeMillis();
		User user = getInfoByAPI(userId);
		long end = System.currentTimeMillis();
		GlobalObject.INFO_LOG.info("USER_INFO_URL costs time " + (end - start) + " ms");
		if(null == user)
			return null;
		
		try
		{
			///保存到redis中
			cacheRedis.saveCache(key, user.toJson().toString(), GlobalConfig.USER_INFO_EXPIRE);
			return user;
		}
		catch(Exception e)
		{	
			throw e;
		}
	}
	
	private static User getInfoByAPI(long userId)
	{
		String url = GlobalConfig.USER_INFO_URL + "?to_uid=" + userId;
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
			
			User user = new User();
			user.setUserId(userId);
			user.setSessionId("");
			user.setNickName(data.optString("nickname", ""));
			user.setAvatar(data.optString("avatar", ""));
			user.setStatus(data.optInt("status", UserStatus.NORMAL));
			user.setType(data.optInt("type", UserType.NORMAL));
			user.setGender(data.optInt("sex", 1));
			return user;
		}
		catch(Exception e)
		{
			return null;
		}
	}
}