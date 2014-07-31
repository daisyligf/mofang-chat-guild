package com.mofang.chat.guild.service.impl;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mofang.chat.guild.component.UserComponent;
import com.mofang.chat.guild.entity.User;
import com.mofang.chat.guild.global.GlobalConfig;
import com.mofang.chat.guild.global.RedisKey;
import com.mofang.chat.guild.redis.GuildGroupUserRedis;
import com.mofang.chat.guild.redis.ResultCacheRedis;
import com.mofang.chat.guild.redis.impl.GuildGroupUserRedisImpl;
import com.mofang.chat.guild.redis.impl.ResultCacheRedisImpl;
import com.mofang.chat.guild.service.GuildGroupUserService;
import com.mofang.framework.util.StringUtil;

/**
 * 
 * @author zhaodx
 *
 */
public class GuildGroupUserServiceImpl implements GuildGroupUserService
{
	private final static GuildGroupUserServiceImpl SERVICE = new GuildGroupUserServiceImpl();
	private GuildGroupUserRedis guildGroupUserRedis = GuildGroupUserRedisImpl.getInstance();
	private ResultCacheRedis resultCacheRedis = ResultCacheRedisImpl.getInstance();
	
	private GuildGroupUserServiceImpl()
	{}
	
	public static GuildGroupUserServiceImpl getInstance()
	{
		return SERVICE;
	}

	@Override
	public JSONArray getUserList(long groupId) throws Exception
	{
		///如果结果缓存里有，直接返回
		String cacheKey = RedisKey.CACHE_GUILD_GROUP_USER_LIST_KEY_PREFIX + groupId;
		String result = resultCacheRedis.getCache(cacheKey);
		if(!StringUtil.isNullOrEmpty(result))
		{
			JSONArray data = new JSONArray(result);
			return data;
		}
		
		///结果缓存没有，则需要重新构建列表信息，并将结果存入缓存中
		JSONArray data = new JSONArray();
		JSONObject userJson = null;
		Map<String, String> memberIds = guildGroupUserRedis.getUserList(groupId);
		if(null != memberIds)
		{
			long memberId = 0L;
			User user = null;
			for(String memberIdStr : memberIds.keySet())
			{
				memberId = Long.parseLong(memberIdStr);
				user = UserComponent.getInfo(memberId);
				if(null == user)
					continue;
				
				userJson = new JSONObject();
				userJson.put("uid", user.getUserId());
				userJson.put("nickname", user.getNickName());
				userJson.put("avatar", user.getAvatar());
				userJson.put("sex", user.getGender());
				data.put(userJson);
			}
		}
		///将结果存入缓存中
		resultCacheRedis.saveCache(cacheKey, data.toString(), GlobalConfig.GUILD_GROUP_USER_LIST_EXPIRE);
		return data;
	}
}