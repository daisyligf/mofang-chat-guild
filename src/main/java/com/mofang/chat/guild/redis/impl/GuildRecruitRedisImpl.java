package com.mofang.chat.guild.redis.impl;

import java.util.Set;

import redis.clients.jedis.Jedis;

import com.mofang.chat.guild.global.GlobalConfig;
import com.mofang.chat.guild.global.GlobalObject;
import com.mofang.chat.guild.global.RedisKey;
import com.mofang.chat.guild.redis.GuildRecruitRedis;
import com.mofang.framework.data.redis.RedisWorker;

/**
 * 
 * @author zhaodx
 *
 */
public class GuildRecruitRedisImpl implements GuildRecruitRedis
{
	private final static GuildRecruitRedisImpl REDIS = new GuildRecruitRedisImpl();
	
	private GuildRecruitRedisImpl()
	{}
	
	public static GuildRecruitRedisImpl getInstance()
	{
		return REDIS;
	}

	@Override
	public boolean addAuditList(final long guildId, final long createTime) throws Exception
	{
		RedisWorker<Boolean> worker = new RedisWorker<Boolean>()
		{
			@Override
			public Boolean execute(Jedis jedis) throws Exception
			{
				String key = RedisKey.GUILD_RECRUIT_AUDIT_LIST_KEY;
				Double score = jedis.zscore(key, String.valueOf(guildId));
				if(null != score)
					return true;
				
				long count = jedis.zcard(key);
				if(count < GlobalConfig.MAX_GUILD_RECRUIT_COUNT)
				{
					jedis.zadd(key, createTime, String.valueOf(guildId));
				}
				else
				{
					///将最老的记录删除掉
					Set<String> guildIds = jedis.zrange(key, 0, (count - GlobalConfig.MAX_GUILD_RECRUIT_COUNT));
					for(String guildIdStr : guildIds)
						jedis.zrem(key, guildIdStr);
							
					///将最新公会的添加到招募列表中
					jedis.zadd(key, createTime, String.valueOf(guildId));	
				}
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
				String key = RedisKey.GUILD_RECRUIT_AUDIT_LIST_KEY;
				jedis.zrem(key, String.valueOf(guildId));
				return true;
			}
		};
		return GlobalObject.REDIS_MASTER_EXECUTOR.execute(worker);
	}

	@Override
	public Set<String> getAuditList() throws Exception
	{
		RedisWorker<Set<String>> worker = new RedisWorker<Set<String>>()
		{
			@Override
			public Set<String> execute(Jedis jedis) throws Exception
			{
				String key = RedisKey.GUILD_RECRUIT_AUDIT_LIST_KEY;
				return jedis.zrevrange(key, 0, GlobalConfig.MAX_GUILD_RECRUIT_COUNT);
			}
		};
		return GlobalObject.REDIS_SLAVE_EXECUTOR.execute(worker);
	}
}