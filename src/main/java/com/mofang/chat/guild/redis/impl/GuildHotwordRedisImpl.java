package com.mofang.chat.guild.redis.impl;

import java.util.Set;

import redis.clients.jedis.Jedis;

import com.mofang.chat.guild.global.GlobalObject;
import com.mofang.chat.guild.global.RedisKey;
import com.mofang.chat.guild.model.GuildHotword;
import com.mofang.chat.guild.redis.GuildHotwordRedis;
import com.mofang.framework.data.redis.RedisWorker;

/**
 * 
 * @author daisyli
 *
 */
public class GuildHotwordRedisImpl implements GuildHotwordRedis
{
    private final static GuildHotwordRedisImpl instance = new GuildHotwordRedisImpl();
    
    private GuildHotwordRedisImpl()
    {}
    
    public static GuildHotwordRedisImpl getInstance()
    {
		return instance;
    }
    
    public boolean add(final GuildHotword model) throws Exception 
    {
	RedisWorker<Boolean> worker = new RedisWorker<Boolean>() 
	{
	    @Override
	    public Boolean execute(Jedis jedis) throws Exception 
	    {
		String key = RedisKey.GUILD_HOTWORD_LIST_KEY;
		jedis.zadd(key, model.getPosition(), model.toJson().toString());
		return true;
	    }
	};
	return GlobalObject.REDIS_MASTER_EXECUTOR.execute(worker);
    }
    
    public boolean del(final GuildHotword model) throws Exception 
    {
	RedisWorker<Boolean> worker = new RedisWorker<Boolean>() 
	{
	    @Override
	    public Boolean execute(Jedis jedis) throws Exception 
	    {
		String key = RedisKey.GUILD_HOTWORD_LIST_KEY;
		jedis.zrem(key, model.toJson().toString());
		return true;
	    }
	};
	return GlobalObject.REDIS_MASTER_EXECUTOR.execute(worker);
    }
    
    public Set<String> list() throws Exception
    {
	RedisWorker<Set<String>> worker = new RedisWorker<Set<String>>()
	{
		@Override
		public Set<String> execute(Jedis jedis) throws Exception
		{
		    	String key = RedisKey.GUILD_HOTWORD_LIST_KEY;
			return jedis.zrevrange(key, 0, Integer.MAX_VALUE);
		}
	};
	return GlobalObject.REDIS_SLAVE_EXECUTOR.execute(worker);
    }
    
    public Integer getPosition(final String word) throws Exception
    {
	RedisWorker<Integer> worker = new RedisWorker<Integer>() 
	{
	    	@Override
	    	public Integer execute(Jedis jedis) throws Exception 
	    	{
	    	    String key = RedisKey.GUILD_HOTWORD_LIST_KEY;
	    	    return jedis.zscore(key, word).intValue();
	    	}
	};
	return GlobalObject.REDIS_SLAVE_EXECUTOR.execute(worker);
    }
    
    
}
