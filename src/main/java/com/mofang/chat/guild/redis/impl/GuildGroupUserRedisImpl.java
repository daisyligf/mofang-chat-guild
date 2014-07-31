package com.mofang.chat.guild.redis.impl;

import java.util.Map;

import redis.clients.jedis.Jedis;

import com.mofang.chat.guild.global.GlobalObject;
import com.mofang.chat.guild.global.RedisKey;
import com.mofang.chat.guild.model.GuildGroupUser;
import com.mofang.chat.guild.redis.GuildGroupUserRedis;
import com.mofang.chat.guild.redis.ResultCacheRedis;
import com.mofang.framework.data.redis.RedisWorker;
import com.mofang.framework.data.redis.workers.DeleteWorker;

/**
 * 
 * @author zhaodx
 *
 */
public class GuildGroupUserRedisImpl implements GuildGroupUserRedis
{
	private final static GuildGroupUserRedisImpl REDIS = new GuildGroupUserRedisImpl();
	private ResultCacheRedis resultCacheRedis = ResultCacheRedisImpl.getInstance();
	
	private GuildGroupUserRedisImpl()
	{}
	
	public static GuildGroupUserRedisImpl getInstance()
	{
		return REDIS;
	}
	
	@Override
	public boolean add(final GuildGroupUser model) throws Exception
	{
		RedisWorker<Boolean> worker = new RedisWorker<Boolean>()
		{
			@Override
			public Boolean execute(Jedis jedis) throws Exception
			{
				String key = RedisKey.GUILD_GROUP_USER_LIST_KEY_PREFIX + model.getGroupId();
				jedis.hset(key, model.getUserId().toString(), model.getReceiveNotify().toString());
				
				///清空公会群组会员缓存信息
				String cacheKey = RedisKey.CACHE_GUILD_GROUP_USER_LIST_KEY_PREFIX + model.getGroupId();
				resultCacheRedis.deleteCache(cacheKey);
				return true;
			}
		};
		return GlobalObject.REDIS_MASTER_EXECUTOR.execute(worker);
	}

	@Override
	public boolean exists(final long groupId, final long userId) throws Exception
	{
		RedisWorker<Boolean> worker = new RedisWorker<Boolean>()
		{
			@Override
			public Boolean execute(Jedis jedis) throws Exception
			{
				String key = RedisKey.GUILD_GROUP_USER_LIST_KEY_PREFIX + groupId;
				return jedis.hexists(key, String.valueOf(userId));
			}
		};
		return GlobalObject.REDIS_MASTER_EXECUTOR.execute(worker);
	}

	@Override
	public boolean updateReceiveNotify(final long groupId, final long userId, final int receiveNotify) throws Exception
	{
		RedisWorker<Boolean> worker = new RedisWorker<Boolean>()
		{
			@Override
			public Boolean execute(Jedis jedis) throws Exception
			{
				String key = RedisKey.GUILD_GROUP_USER_LIST_KEY_PREFIX + groupId;
				jedis.hset(key, String.valueOf(userId), String.valueOf(receiveNotify));
				return true;
			}
		};
		return GlobalObject.REDIS_MASTER_EXECUTOR.execute(worker);
	}

	@Override
	public String getRecevieNotify(final long groupId, final long userId) throws Exception
	{
		RedisWorker<String> worker = new RedisWorker<String>()
		{
			@Override
			public String execute(Jedis jedis) throws Exception
			{
				String key = RedisKey.GUILD_GROUP_USER_LIST_KEY_PREFIX + groupId;
				return jedis.hget(key, String.valueOf(userId));
			}
		};
		return GlobalObject.REDIS_SLAVE_EXECUTOR.execute(worker);
	}

	@Override
	public boolean delete(final long groupId, final long userId) throws Exception
	{
		RedisWorker<Boolean> worker = new RedisWorker<Boolean>()
		{
			@Override
			public Boolean execute(Jedis jedis) throws Exception
			{
				String key = RedisKey.GUILD_GROUP_USER_LIST_KEY_PREFIX + groupId;
				jedis.hdel(key, String.valueOf(userId));
				
				///清空公会群组会员缓存信息
				String cacheKey = RedisKey.CACHE_GUILD_GROUP_USER_LIST_KEY_PREFIX + groupId;
				resultCacheRedis.deleteCache(cacheKey);
				return true;
			}
		};
		return GlobalObject.REDIS_MASTER_EXECUTOR.execute(worker);
	}

	@Override
	public boolean deleteByGroup(long groupId) throws Exception
	{
		///清空公会群组会员缓存信息
		String cacheKey = RedisKey.CACHE_GUILD_GROUP_USER_LIST_KEY_PREFIX + groupId;
		resultCacheRedis.deleteCache(cacheKey);
		
		String key = RedisKey.GUILD_GROUP_USER_LIST_KEY_PREFIX + groupId;
		RedisWorker<Boolean> worker = new  DeleteWorker(key);
		return GlobalObject.REDIS_MASTER_EXECUTOR.execute(worker);
	}

	@Override
	public Map<String, String> getUserList(final long groupId) throws Exception
	{
		RedisWorker<Map<String, String>> worker = new RedisWorker<Map<String, String>>()
		{
			@Override
			public Map<String, String> execute(Jedis jedis) throws Exception
			{
				String key = RedisKey.GUILD_GROUP_USER_LIST_KEY_PREFIX + groupId;
				return jedis.hgetAll(key);
			}
		};
		return GlobalObject.REDIS_SLAVE_EXECUTOR.execute(worker);
	}

	@Override
	public long getUserCount(final long groupId) throws Exception
	{
		RedisWorker<Long> worker = new RedisWorker<Long>()
		{
			@Override
			public Long execute(Jedis jedis) throws Exception
			{
				String key = RedisKey.GUILD_GROUP_USER_LIST_KEY_PREFIX + groupId;
				Long count = jedis.hlen(key);
				return count == null ? 0L : count;
			}
		};
		return GlobalObject.REDIS_SLAVE_EXECUTOR.execute(worker);
	}
}