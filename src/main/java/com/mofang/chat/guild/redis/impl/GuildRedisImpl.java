package com.mofang.chat.guild.redis.impl;

import java.util.Set;

import org.json.JSONObject;

import redis.clients.jedis.Jedis;

import com.mofang.chat.guild.global.GlobalConfig;
import com.mofang.chat.guild.global.GlobalObject;
import com.mofang.chat.guild.global.RedisKey;
import com.mofang.chat.guild.global.common.GuildStatus;
import com.mofang.chat.guild.model.Guild;
import com.mofang.chat.guild.redis.GuildRedis;
import com.mofang.chat.guild.redis.GuildUserRedis;
import com.mofang.chat.guild.redis.ResultCacheRedis;
import com.mofang.framework.data.redis.RedisWorker;
import com.mofang.framework.data.redis.workers.GetWorker;
import com.mofang.framework.data.redis.workers.IncrWorker;
import com.mofang.framework.data.redis.workers.SetWorker;
import com.mofang.framework.util.StringUtil;

/**
 * 
 * @author zhaodx
 *
 */
public class GuildRedisImpl implements GuildRedis
{
	private final static GuildRedisImpl REDIS = new GuildRedisImpl();
	private GuildUserRedis guildUserRedis = GuildUserRedisImpl.getInstance();
	private ResultCacheRedis resultCacheRedis = ResultCacheRedisImpl.getInstance();
	
	private GuildRedisImpl()
	{}
	
	public static GuildRedisImpl getInstance()
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
				String key = RedisKey.GUILD_ID_INCREMENT_KEY;
				boolean exists = jedis.exists(key);
				if(!exists)
				{
					///初始化公会ID起始值
					jedis.set(key, String.valueOf(GlobalConfig.GUILD_ID_START));
				}
				return true;
			}
		};
		return GlobalObject.REDIS_MASTER_EXECUTOR.execute(worker);
	}

	@Override
	public Long getMaxId() throws Exception
	{
		RedisWorker<Long> worker = new IncrWorker(RedisKey.GUILD_ID_INCREMENT_KEY);
		return GlobalObject.REDIS_MASTER_EXECUTOR.execute(worker);
	}

	@Override
	public boolean save(final Guild model) throws Exception
	{
		RedisWorker<Boolean> worker = new RedisWorker<Boolean>()
		{
			@Override
			public Boolean execute(Jedis jedis) throws Exception
			{
				String infoKey = RedisKey.GUILD_INFO_KEY_PREFIX + model.getGuildId();
				String newListKey = RedisKey.GUILD_NEW_LIST_KEY;
				String hotListKey = RedisKey.GUILD_HOT_LIST_KEY;
				
				///将公会信息添加到redis中
				JSONObject json = model.toJson();
				jedis.set(infoKey, json.toString());
				
				///将公会ID添加到最新公会列表中
				jedis.zadd(newListKey, model.getCreateTime().getTime(), model.getGuildId().toString());
				
				///将公会ID添加到最热公会列表中
				jedis.zadd(hotListKey, model.getHot(), model.getGuildId().toString());
				
				///将公会ID添加到我的公会列表中
				String myListKey = RedisKey.MY_GUILD_LIST_KEY_PREFIX + model.getCreatorId();
				///我自己创建的公会要排序靠前
				long score = System.currentTimeMillis() * 2;
				jedis.zadd(myListKey, score, model.getGuildId().toString());
				
				///清空我的公会列表结果缓存
				String cacheKey = RedisKey.CACHE_MY_GUILD_LIST_KEY_PREFIX + model.getCreatorId();
				resultCacheRedis.deleteCache(cacheKey);
				return true;
			}
		};
		return GlobalObject.REDIS_MASTER_EXECUTOR.execute(worker);
	}

	@Override
	public boolean update(Guild model) throws Exception
	{
		///清空公会信息缓存
		String cacheKey = null;
		///清空公会信息结果缓存
		cacheKey = RedisKey.CACHE_GUILD_INFO_KEY_PREFIX + model.getGuildId();
		resultCacheRedis.deleteCache(cacheKey);
		///清空我的公会列表结果缓存
		cacheKey = RedisKey.CACHE_MY_GUILD_LIST_KEY_PREFIX + model.getCreatorId();
		resultCacheRedis.deleteCache(cacheKey);
		
		String key = RedisKey.GUILD_INFO_KEY_PREFIX + model.getGuildId();
		JSONObject json = model.toJson();
		RedisWorker<Boolean> worker = new SetWorker(key, json.toString());
		return GlobalObject.REDIS_MASTER_EXECUTOR.execute(worker);
	}

	@Override
	public boolean delete(final long guildId) throws Exception
	{
		RedisWorker<Boolean> worker = new RedisWorker<Boolean>()
		{
			@Override
			public Boolean execute(Jedis jedis) throws Exception
			{
				String infoKey = RedisKey.GUILD_INFO_KEY_PREFIX + guildId;
				///删除公会信息
				jedis.del(infoKey);
				
				///删除公会缓存信息
				String cacheKey = RedisKey.CACHE_GUILD_INFO_KEY_PREFIX + guildId;
				resultCacheRedis.deleteCache(cacheKey);
				
				///从最新和最热的列表中删除公会
				String newListKey = RedisKey.GUILD_NEW_LIST_KEY;
				String hotListKey = RedisKey.GUILD_HOT_LIST_KEY;
				jedis.zrem(newListKey, String.valueOf(guildId));
				jedis.zrem(hotListKey, String.valueOf(guildId));
				
				///根据guildID获取成员列表，遍历成员列表，将该公会从指定会员的"我的公会"中删除
				Set<String> userIds = guildUserRedis.getUserList(guildId);
				String myListKey = null;
				for(String userId : userIds)
				{
					myListKey = RedisKey.MY_GUILD_LIST_KEY_PREFIX + userId;
					jedis.zrem(myListKey, String.valueOf(guildId));
					
					///删除"我的公会"缓存信息
					cacheKey = RedisKey.CACHE_MY_GUILD_LIST_KEY_PREFIX + userId;
					resultCacheRedis.deleteCache(cacheKey);
				}
				return true;
			}
		};
		return GlobalObject.REDIS_MASTER_EXECUTOR.execute(worker);
	}

	@Override
	public boolean updateStatus(final long guildId, final int status) throws Exception
	{
		RedisWorker<Boolean> worker = new RedisWorker<Boolean>()
		{
			@Override
			public Boolean execute(Jedis jedis) throws Exception
			{
				Guild model = getInfo(guildId);
				if(null == model)
					return false;
				
				if(model.getStatus() != status)
				{
					///更新实体信息
					model.setStatus(status);
					update(model);
					
					String hotListKey = RedisKey.GUILD_HOT_LIST_KEY;
					String newListKey = RedisKey.GUILD_NEW_LIST_KEY;
					
					if(status == GuildStatus.CLOSEDOWN)
					{
						///将公会从最新、最热以及我的公会列表中删除
						jedis.zrem(hotListKey, String.valueOf(guildId));
						jedis.zrem(newListKey, String.valueOf(guildId));
						
						///根据guildID获取成员列表，遍历成员列表，将该公会从指定会员的"我的公会"中删除
						Set<String> userIds = guildUserRedis.getUserList(guildId);
						String myListKey = null;
						for(String userId : userIds)
						{
							myListKey = RedisKey.MY_GUILD_LIST_KEY_PREFIX + userId;
							jedis.zrem(myListKey, String.valueOf(guildId));
						}
					}
					else if(status == GuildStatus.NORMAL)
					{
						///根据guildID获取成员列表，遍历成员列表，将该公会添加到指定会员的"我的公会"中
						Set<String> userIds = guildUserRedis.getUserList(guildId);
						String myListKey = null;
						for(String userId : userIds)
						{
							myListKey = RedisKey.MY_GUILD_LIST_KEY_PREFIX + userId;
							jedis.zadd(myListKey, model.getCreateTime().getTime(), String.valueOf(guildId));
						}
					}
				}
				return true;
			}
		};
		return GlobalObject.REDIS_MASTER_EXECUTOR.execute(worker);
	}

	@Override
	public boolean updateHotSequence(final long guildId, final long sequence, final double hot) throws Exception
	{
		RedisWorker<Boolean> worker = new RedisWorker<Boolean>()
		{
			@Override
			public Boolean execute(Jedis jedis) throws Exception
			{
				String key = RedisKey.GUILD_HOT_LIST_KEY;
				jedis.zadd(key, sequence + hot, String.valueOf(guildId));
				return true;
			}
		};
		return GlobalObject.REDIS_MASTER_EXECUTOR.execute(worker);
	}

	@Override
	public boolean updateNewSequence(final long guildId, final long sequence, final long createtime) throws Exception
	{
		RedisWorker<Boolean> worker = new RedisWorker<Boolean>()
		{
			@Override
			public Boolean execute(Jedis jedis) throws Exception
			{
				String key = RedisKey.GUILD_NEW_LIST_KEY;
				jedis.zadd(key, sequence + createtime, String.valueOf(guildId));
				return true;
			}
		};
		return GlobalObject.REDIS_MASTER_EXECUTOR.execute(worker);
	}

	@Override
	public boolean updateHotList(final long guildId, final double score) throws Exception
	{
		RedisWorker<Boolean> worker = new RedisWorker<Boolean>()
		{
			@Override
			public Boolean execute(Jedis jedis) throws Exception
			{
				String key = RedisKey.GUILD_HOT_LIST_KEY;
				jedis.zadd(key, score, String.valueOf(guildId));
				return true;
			}
		};
		return GlobalObject.REDIS_MASTER_EXECUTOR.execute(worker);
	}

	@Override
	public Guild getInfo(final long guildId) throws Exception
	{
		String key = RedisKey.GUILD_INFO_KEY_PREFIX + guildId;
		RedisWorker<String> worker = new GetWorker(key);
		String value = GlobalObject.REDIS_SLAVE_EXECUTOR.execute(worker);
		if(StringUtil.isNullOrEmpty(value))
			return null;
		
		JSONObject json = new JSONObject(value);
		return Guild.buildByJson(json);
	}

	@Override
	public Set<String> getHotList(final int start, final int end) throws Exception
	{
		RedisWorker<Set<String>> worker = new RedisWorker<Set<String>>()
		{
			@Override
			public Set<String> execute(Jedis jedis) throws Exception
			{
				String key = RedisKey.GUILD_HOT_LIST_KEY;
				return jedis.zrevrange(key, start, end);
			}
		};
		return GlobalObject.REDIS_SLAVE_EXECUTOR.execute(worker);
	}

	@Override
	public long getHotCount() throws Exception
	{
		RedisWorker<Long> worker = new RedisWorker<Long>()
		{
			@Override
			public Long execute(Jedis jedis) throws Exception
			{
				String key = RedisKey.GUILD_HOT_LIST_KEY;
				return jedis.zcard(key);
			}
		};
		return GlobalObject.REDIS_SLAVE_EXECUTOR.execute(worker);
	}

	@Override
	public Set<String> getNewList(final int start, final int end) throws Exception
	{
		RedisWorker<Set<String>> worker = new RedisWorker<Set<String>>()
		{
			@Override
			public Set<String> execute(Jedis jedis) throws Exception
			{
				String key = RedisKey.GUILD_NEW_LIST_KEY;
				return jedis.zrevrange(key, start, end);
			}
		};
		return GlobalObject.REDIS_SLAVE_EXECUTOR.execute(worker);
	}

	@Override
	public long getNewCount() throws Exception
	{
		RedisWorker<Long> worker = new RedisWorker<Long>()
		{
			@Override
			public Long execute(Jedis jedis) throws Exception
			{
				String key = RedisKey.GUILD_NEW_LIST_KEY;
				return jedis.zcard(key);
			}
		};
		return GlobalObject.REDIS_SLAVE_EXECUTOR.execute(worker);
	}

	@Override
	public Set<String> getMyList(final long userId) throws Exception
	{
		RedisWorker<Set<String>> worker = new RedisWorker<Set<String>>()
		{
			@Override
			public Set<String> execute(Jedis jedis) throws Exception
			{
				String key = RedisKey.MY_GUILD_LIST_KEY_PREFIX + userId;
				return jedis.zrevrange(key, 0, Integer.MAX_VALUE);
			}
		};
		return GlobalObject.REDIS_SLAVE_EXECUTOR.execute(worker);
	}

	@Override
	public long getRank(final long guildId) throws Exception
	{
		RedisWorker<Long> worker = new RedisWorker<Long>()
		{
			@Override
			public Long execute(Jedis jedis) throws Exception
			{
				String key = RedisKey.GUILD_HOT_LIST_KEY;
				return jedis.zrevrank(key, String.valueOf(guildId));
			}
		};
		return GlobalObject.REDIS_SLAVE_EXECUTOR.execute(worker);
	}
}