package com.mofang.chat.guild.redis.impl;

import java.util.Set;

import redis.clients.jedis.Jedis;

import com.mofang.chat.guild.global.GlobalObject;
import com.mofang.chat.guild.global.RedisKey;
import com.mofang.chat.guild.model.GuildGame;
import com.mofang.chat.guild.redis.GuildGameRedis;
import com.mofang.chat.guild.redis.ResultCacheRedis;
import com.mofang.framework.data.redis.RedisWorker;

/**
 * 
 * @author zhaodx
 *
 */
public class GuildGameRedisImpl implements GuildGameRedis
{
	private final static GuildGameRedisImpl REDIS = new GuildGameRedisImpl();
	private ResultCacheRedis resultCacheRedis = ResultCacheRedisImpl.getInstance();
	
	private GuildGameRedisImpl()
	{}
	
	public static GuildGameRedisImpl getInstance()
	{
		return REDIS;
	}

	@Override
	public boolean add(final GuildGame model) throws Exception
	{
		RedisWorker<Boolean> worker = new RedisWorker<Boolean>()
		{
			@Override
			public Boolean execute(Jedis jedis) throws Exception
			{
				String guildGamesKey = RedisKey.GUILD_GAME_LIST_KEY_PREFIX + model.getGuildId();
				String gameGuildsKey = RedisKey.GAME_GUILD_LIST_KEY_PREFIX + model.getGameId();
				
				///添加公会和游戏的对应关系
				jedis.zadd(guildGamesKey, 0, String.valueOf(model.getGameId()));
				///添加游戏和公会的对应关系
				jedis.zadd(gameGuildsKey, 0, String.valueOf(model.getGuildId()));
				
				///清空公会信息缓存
				String cacheKey = RedisKey.CACHE_GUILD_INFO_KEY_PREFIX + model.getGuildId();
				resultCacheRedis.deleteCache(cacheKey);
				return true;
			}
		};
		return GlobalObject.REDIS_MASTER_EXECUTOR.execute(worker);
	}

	@Override
	public boolean delete(final long guildId, final int gameId) throws Exception
	{
		RedisWorker<Boolean> worker = new RedisWorker<Boolean>()
		{
			@Override
			public Boolean execute(Jedis jedis) throws Exception
			{
				String guildGamesKey = RedisKey.GUILD_GAME_LIST_KEY_PREFIX + guildId;
				String gameGuildsKey = RedisKey.GAME_GUILD_LIST_KEY_PREFIX + gameId;
				
				///删除公会和游戏的对应关系
				jedis.zrem(guildGamesKey, String.valueOf(gameId));
				///删除游戏和公会的对应关系
				jedis.zrem(gameGuildsKey, String.valueOf(guildId));
				
				///清空公会信息缓存
				String cacheKey = RedisKey.CACHE_GUILD_INFO_KEY_PREFIX + guildId;
				resultCacheRedis.deleteCache(cacheKey);
				return true;
			}
		};
		return GlobalObject.REDIS_MASTER_EXECUTOR.execute(worker);
	}

	@Override
	public Set<String> getGameListByGuild(final long guildId) throws Exception
	{
		RedisWorker<Set<String>> worker = new RedisWorker<Set<String>>()
		{
			@Override
			public Set<String> execute(Jedis jedis) throws Exception
			{
				String key = RedisKey.GUILD_GAME_LIST_KEY_PREFIX + guildId;
				return jedis.zrevrange(key, 0, Integer.MAX_VALUE);
			}
		};
		return GlobalObject.REDIS_SLAVE_EXECUTOR.execute(worker);
	}

	@Override
	public long getGameCountByGuild(final long guildId) throws Exception
	{
		RedisWorker<Long> worker = new RedisWorker<Long>()
		{
			@Override
			public Long execute(Jedis jedis) throws Exception
			{
				String key = RedisKey.GUILD_GAME_LIST_KEY_PREFIX + guildId;
				return jedis.zcard(key);
			}
		};
		return GlobalObject.REDIS_SLAVE_EXECUTOR.execute(worker);
	}

	@Override
	public Set<String> getGuildListByGame(final int gameId, final int start, final int end) throws Exception
	{
		RedisWorker<Set<String>> worker = new RedisWorker<Set<String>>()
		{
			@Override
			public Set<String> execute(Jedis jedis) throws Exception
			{
				String key = RedisKey.GAME_GUILD_LIST_KEY_PREFIX + gameId;
				return jedis.zrevrange(key, start, end);
			}
		};
		return GlobalObject.REDIS_SLAVE_EXECUTOR.execute(worker);
	}

	@Override
	public long getGuildCountByGame(final int gameId) throws Exception
	{
		RedisWorker<Long> worker = new RedisWorker<Long>()
		{
			@Override
			public Long execute(Jedis jedis) throws Exception
			{
				String key = RedisKey.GAME_GUILD_LIST_KEY_PREFIX + gameId;
				return jedis.zcard(key);
			}
		};
		return GlobalObject.REDIS_SLAVE_EXECUTOR.execute(worker);
	}
}