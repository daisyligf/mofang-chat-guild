package com.mofang.chat.guild.init.impl;

import com.mofang.chat.guild.init.AbstractInitializer;
import com.mofang.chat.guild.redis.GuildGroupRedis;
import com.mofang.chat.guild.redis.GuildRedis;
import com.mofang.chat.guild.redis.impl.GuildGroupRedisImpl;
import com.mofang.chat.guild.redis.impl.GuildRedisImpl;

/**
 * 
 * @author zhaodx
 *
 */
public class RedisDataInitializer extends AbstractInitializer
{
	private GuildRedis guildRedis = GuildRedisImpl.getInstance();
	private GuildGroupRedis guildGroupRedis = GuildGroupRedisImpl.getInstance();
	
	@Override
	public void load() throws Exception
	{
		guildRedis.initMaxId();
		guildGroupRedis.initMaxId();
	}
}