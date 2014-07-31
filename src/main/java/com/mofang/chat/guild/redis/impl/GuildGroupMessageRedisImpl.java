package com.mofang.chat.guild.redis.impl;

import org.json.JSONObject;

import redis.clients.jedis.Jedis;

import com.mofang.chat.guild.global.GlobalObject;
import com.mofang.chat.guild.global.RedisKey;
import com.mofang.chat.guild.redis.GuildGroupMessageRedis;
import com.mofang.framework.data.redis.RedisWorker;
import com.mofang.framework.util.StringUtil;

/**
 * 
 * @author zhaodx
 *
 */
public class GuildGroupMessageRedisImpl implements GuildGroupMessageRedis
{
	private final static GuildGroupMessageRedisImpl REDIS = new GuildGroupMessageRedisImpl();
	
	private GuildGroupMessageRedisImpl()
	{}
	
	public static GuildGroupMessageRedisImpl getInstance()
	{
		return REDIS;
	}

	@Override
	public int getUnreadCount(final long userId, final long groupId) throws Exception
	{
		RedisWorker<Integer> worker = new RedisWorker<Integer>()
		{
			@Override
			public Integer execute(Jedis jedis) throws Exception 
			{
				String key = RedisKey.USER_GROUP_UNREAD_KEY_PREFIX + userId;
				String value = jedis.hget(key, String.valueOf(groupId));
				if(StringUtil.isNullOrEmpty(value))
					return 0;
				
				JSONObject json = new JSONObject(value);
				return json.optInt("unread_count", 0);
			}
		};
		return GlobalObject.CHAT_SLAVE_EXECUTOR.execute(worker);
	}
}
