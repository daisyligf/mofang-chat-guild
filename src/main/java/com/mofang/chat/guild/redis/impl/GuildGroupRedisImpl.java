package com.mofang.chat.guild.redis.impl;

import java.util.Set;

import org.json.JSONObject;

import redis.clients.jedis.Jedis;

import com.mofang.chat.guild.global.GlobalConfig;
import com.mofang.chat.guild.global.GlobalObject;
import com.mofang.chat.guild.global.RedisKey;
import com.mofang.chat.guild.global.common.GuildGroupType;
import com.mofang.chat.guild.model.GuildGroup;
import com.mofang.chat.guild.redis.GuildGroupRedis;
import com.mofang.framework.data.redis.RedisWorker;
import com.mofang.framework.data.redis.workers.GetWorker;
import com.mofang.framework.data.redis.workers.IncrWorker;
import com.mofang.framework.util.StringUtil;

/**
 * 
 * @author zhaodx
 *
 */
public class GuildGroupRedisImpl implements GuildGroupRedis
{
	private final static GuildGroupRedisImpl REDIS = new GuildGroupRedisImpl();
	
	private GuildGroupRedisImpl()
	{}
	
	public static GuildGroupRedisImpl getInstance()
	{
		return REDIS;
	}

	@Override
	public boolean initMaxId() throws Exception
	{
		RedisWorker<Boolean> worker = new RedisWorker<Boolean>()
		{
			@Override
			public Boolean execute(Jedis jedis) throws Exception
			{
				String key = RedisKey.GUILD_GROUP_ID_INCREMENT_KEY;
				boolean exists = jedis.exists(key);
				if(!exists)
				{
					///初始化公会群组ID起始值
					jedis.set(key, String.valueOf(GlobalConfig.GUILD_GROUP_ID_START));
				}
				return true;
			}
		};
		return GlobalObject.REDIS_MASTER_EXECUTOR.execute(worker);
	}

	@Override
	public long getMaxId() throws Exception
	{
		RedisWorker<Long> worker = new IncrWorker(RedisKey.GUILD_GROUP_ID_INCREMENT_KEY);
		return GlobalObject.REDIS_MASTER_EXECUTOR.execute(worker);
	}

	@Override
	public boolean add(final GuildGroup model) throws Exception
	{
		RedisWorker<Boolean> worker = new RedisWorker<Boolean>()
		{
			@Override
			public Boolean execute(Jedis jedis) throws Exception
			{
				String infoKey = RedisKey.GUILD_GROUP_INFO_KEY_PREFIX + model.getGroupId();
				///将公会群组信息添加到redis中
				JSONObject json = model.toJson();
				jedis.set(infoKey, json.toString());
				
				///将公会群组添加到公会/公会游戏对应的群组列表中
				if(model.getType() == GuildGroupType.GUILD)
				{
					String guildGroupKey = RedisKey.GUILD_GROUP_LIST_KEY_PREFIX + model.getGuildId();
					jedis.zadd(guildGroupKey, model.getCreateTime().getTime(), String.valueOf(model.getGroupId()));
				}
				else if(model.getType() == GuildGroupType.GUILD_GAME)
				{
					String guildGameGroupKey = RedisKey.GUILD_GAME_GROUP_LIST_KEY_PREFIX + model.getGuildId() + "_" + model.getGameId();
					jedis.zadd(guildGameGroupKey, model.getCreateTime().getTime(), String.valueOf(model.getGroupId()));
				}
				return true;
			}
		};
		return GlobalObject.REDIS_MASTER_EXECUTOR.execute(worker);
	}

	@Override
	public boolean delete(final long groupId) throws Exception
	{
		RedisWorker<Boolean> worker = new RedisWorker<Boolean>()
		{
			@Override
			public Boolean execute(Jedis jedis) throws Exception
			{
				String key = RedisKey.GUILD_GROUP_INFO_KEY_PREFIX + groupId;
				jedis.del(key);
				return true;
			}
		};
		return GlobalObject.REDIS_MASTER_EXECUTOR.execute(worker);
	}

	@Override
	public boolean deleteByGuild(final long guildId) throws Exception
	{
		RedisWorker<Boolean> worker = new RedisWorker<Boolean>()
		{
			@Override
			public Boolean execute(Jedis jedis) throws Exception
			{
				String key = RedisKey.GUILD_GROUP_LIST_KEY_PREFIX + guildId;
				jedis.del(key);
				return true;
			}
		};
		return GlobalObject.REDIS_MASTER_EXECUTOR.execute(worker);
	}

	@Override
	public boolean deleteByGame(final long guildId, final int gameId) throws Exception
	{
		RedisWorker<Boolean> worker = new RedisWorker<Boolean>()
		{
			@Override
			public Boolean execute(Jedis jedis) throws Exception
			{
				String key = RedisKey.GUILD_GAME_GROUP_LIST_KEY_PREFIX + guildId + "_" + gameId;
				jedis.del(key);
				return true;
			}
		};
		return GlobalObject.REDIS_MASTER_EXECUTOR.execute(worker);
	}

	@Override
	public GuildGroup getInfo(long groupId) throws Exception
	{
		String key = RedisKey.GUILD_GROUP_INFO_KEY_PREFIX + groupId;
		RedisWorker<String> worker = new GetWorker(key);
		String value = GlobalObject.REDIS_SLAVE_EXECUTOR.execute(worker);
		if(StringUtil.isNullOrEmpty(value))
			return null;
		
		JSONObject json = new JSONObject(value);
		return GuildGroup.buildByJson(json);
	}

	@Override
	public Set<String> getGuildGroupList(final long guildId) throws Exception
	{
		RedisWorker<Set<String>> worker = new RedisWorker<Set<String>>()
		{
			@Override
			public Set<String> execute(Jedis jedis) throws Exception
			{
				String key = RedisKey.GUILD_GROUP_LIST_KEY_PREFIX + guildId;
				return jedis.zrevrange(key, 0, Integer.MAX_VALUE);
			}
		};
		return GlobalObject.REDIS_SLAVE_EXECUTOR.execute(worker);
	}

	@Override
	public Set<String> getGuildGameGroupList(final long guildId, final int gameId) throws Exception
	{
		RedisWorker<Set<String>> worker = new RedisWorker<Set<String>>()
		{
			@Override
			public Set<String> execute(Jedis jedis) throws Exception
			{
				String key = RedisKey.GUILD_GAME_GROUP_LIST_KEY_PREFIX + guildId + "_" + gameId;
				return jedis.zrevrange(key, 0, Integer.MAX_VALUE);
			}
		};
		return GlobalObject.REDIS_SLAVE_EXECUTOR.execute(worker);
	}
}