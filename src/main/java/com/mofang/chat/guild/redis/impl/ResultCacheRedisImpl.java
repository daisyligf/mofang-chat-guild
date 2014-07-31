package com.mofang.chat.guild.redis.impl;

import redis.clients.jedis.Jedis;

import com.mofang.chat.guild.global.GlobalObject;
import com.mofang.chat.guild.redis.ResultCacheRedis;
import com.mofang.framework.data.redis.RedisWorker;
import com.mofang.framework.data.redis.workers.DeleteWorker;
import com.mofang.framework.data.redis.workers.GetWorker;

/**
 * 
 * @author zhaodx
 *
 */
public class ResultCacheRedisImpl implements ResultCacheRedis
{
	private final static ResultCacheRedisImpl REDIS = new ResultCacheRedisImpl();
	
	private ResultCacheRedisImpl()
	{}
	
	public static ResultCacheRedisImpl getInstance()
	{
		return REDIS;
	}

	@Override
	public boolean saveCache(final String key, final String value, final int expire) throws Exception
	{
		RedisWorker<Boolean> worker = new RedisWorker<Boolean>()
		{
			@Override
			public Boolean execute(Jedis jedis) throws Exception 
			{
				jedis.set(key, value);
				jedis.expire(key, expire);
				return true;
			}
		};
		return GlobalObject.REDIS_MASTER_EXECUTOR.execute(worker);
	}

	@Override
	public boolean deleteCache(String key) throws Exception
	{
		RedisWorker<Boolean> worker = new DeleteWorker(key);
		return GlobalObject.REDIS_MASTER_EXECUTOR.execute(worker);
	}

	@Override
	public String getCache(String key) throws Exception
	{
		RedisWorker<String> worker = new GetWorker(key);
		return GlobalObject.REDIS_SLAVE_EXECUTOR.execute(worker);
	}
}