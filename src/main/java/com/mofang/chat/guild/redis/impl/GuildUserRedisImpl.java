package com.mofang.chat.guild.redis.impl;

import java.util.Date;
import java.util.Set;

import org.json.JSONObject;

import redis.clients.jedis.Jedis;

import com.mofang.chat.guild.global.GlobalObject;
import com.mofang.chat.guild.global.RedisKey;
import com.mofang.chat.guild.global.common.GuildUserRole;
import com.mofang.chat.guild.global.common.GuildUserStatus;
import com.mofang.chat.guild.model.GuildUser;
import com.mofang.chat.guild.redis.GuildUserRedis;
import com.mofang.chat.guild.redis.ResultCacheRedis;
import com.mofang.framework.data.redis.RedisWorker;
import com.mofang.framework.data.redis.workers.DeleteWorker;
import com.mofang.framework.data.redis.workers.GetWorker;
import com.mofang.framework.util.StringUtil;

/**
 * 
 * @author zhaodx
 *
 */
public class GuildUserRedisImpl implements GuildUserRedis
{
	private final static GuildUserRedisImpl REDIS = new GuildUserRedisImpl();
	private ResultCacheRedis resultCacheRedis = ResultCacheRedisImpl.getInstance();
	
	private GuildUserRedisImpl()
	{}
	
	public static GuildUserRedisImpl getInstance()
	{
		return REDIS;
	}

	@Override
	public boolean exists(final long guildId, final long userId) throws Exception
	{
		RedisWorker<Boolean> worker = new RedisWorker<Boolean>()
		{
			@Override
			public Boolean execute(Jedis jedis) throws Exception
			{
				String key = RedisKey.GUILD_USER_LIST_KEY_PREFIX + guildId;
				return jedis.sismember(key, String.valueOf(userId));
			}
		};
		return GlobalObject.REDIS_SLAVE_EXECUTOR.execute(worker);
	}

	@Override
	public boolean existsUnAudit(final long guildId, final long userId) throws Exception
	{
		RedisWorker<Boolean> worker = new RedisWorker<Boolean>()
		{
			@Override
			public Boolean execute(Jedis jedis) throws Exception
			{
				String key = RedisKey.GUILD_USER_UNAUDITED_LIST_KEY_PREFIX + guildId;
				return jedis.sismember(key, String.valueOf(userId));
			}
		};
		return GlobalObject.REDIS_SLAVE_EXECUTOR.execute(worker);
	}

	/**
	 * 每个用户只能加入固定数目的公会
	 */
	@Override
	public boolean add(final GuildUser model) throws Exception
	{
		RedisWorker<Boolean> worker = new RedisWorker<Boolean>()
		{
			@Override
			public Boolean execute(Jedis jedis) throws Exception
			{
				String infoKey = RedisKey.GUILD_USER_INFO_KEY_PREFIX + model.getGuildId() + "_" + model.getUserId();
				String unauditedListKey = RedisKey.GUILD_USER_UNAUDITED_LIST_KEY_PREFIX + model.getGuildId();
				String memberListKey = RedisKey.GUILD_USER_LIST_KEY_PREFIX + model.getGuildId();
				
				///将公会会员信息添加到redis中
				JSONObject json = model.toJson();
				jedis.set(infoKey, json.toString());
				
				if(model.getRole() != GuildUserRole.CHAIRMAN)
				{
					///将会员ID添加到公会待审核会员列表中
					jedis.sadd(unauditedListKey, String.valueOf(model.getUserId()));
				}
				else
				{
					jedis.sadd(memberListKey, String.valueOf(model.getUserId()));
				}
				
				///清空公会会员缓存信息
				String cacheKey = RedisKey.CACHE_GUILD_USER_LIST_KEY_PREFIX + model.getGuildId();
				resultCacheRedis.deleteCache(cacheKey);
				return true;
			}
		};
		return GlobalObject.REDIS_MASTER_EXECUTOR.execute(worker);
	}

	@Override
	public boolean delete(final long guildId, final long userId) throws Exception
	{
		RedisWorker<Boolean> worker = new RedisWorker<Boolean>()
		{
			@Override
			public Boolean execute(Jedis jedis) throws Exception
			{
				String infoKey = RedisKey.GUILD_USER_INFO_KEY_PREFIX + guildId + "_" + userId;
				String memberlistKey = RedisKey.GUILD_USER_LIST_KEY_PREFIX + guildId;
				String myGuildListKey = RedisKey.MY_GUILD_LIST_KEY_PREFIX + userId;
				
				///将公会会员信息从redis中删除
				jedis.del(infoKey);
				
				///将会员ID从公会会员列表中删除
				jedis.srem(memberlistKey, String.valueOf(userId));
				
				///将公会从用户的“我的公会”中删除
				jedis.zrem(myGuildListKey, String.valueOf(guildId));
				
				///清空公会会员缓存信息
				String cacheKey = RedisKey.CACHE_GUILD_USER_LIST_KEY_PREFIX + guildId;
				resultCacheRedis.deleteCache(cacheKey);
				
				///清空公会缓存信息
				cacheKey = RedisKey.CACHE_GUILD_INFO_KEY_PREFIX + guildId;
				resultCacheRedis.deleteCache(cacheKey);
				
				///清空用户“我的公会”缓存信息
				cacheKey = RedisKey.CACHE_MY_GUILD_LIST_KEY_PREFIX + userId;
				resultCacheRedis.deleteCache(cacheKey);
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
				///获取公会成员列表
				Set<String> userIds = getUserList(guildId);
				if(null != userIds)
				{
					///删除公会成员
					for(String userId : userIds)
						delete(guildId, Long.parseLong(userId));
				}
				
				///获取公会待审核成员列表
				Set<String> unauditedUserIds = getUnauditedUserList(guildId);
				if(null != unauditedUserIds)
				{
					String infoKey = null;
					///删除公会待审核成员
					for(String userId : unauditedUserIds)
					{
						infoKey = RedisKey.GUILD_USER_INFO_KEY_PREFIX + guildId + "_" + userId;
						jedis.del(infoKey);
					}
				}
						
				///删除成员列表和待审核成员列表
				String userListKey = RedisKey.GUILD_USER_LIST_KEY_PREFIX + guildId;
				String unauditedUserListKey = RedisKey.GUILD_USER_LIST_KEY_PREFIX + guildId;
				jedis.del(userListKey);
				jedis.del(unauditedUserListKey);
				
				///清空公会会员缓存信息
				String cacheKey = RedisKey.CACHE_GUILD_USER_LIST_KEY_PREFIX + guildId;
				resultCacheRedis.deleteCache(cacheKey);
				return true;
			}
		};
		return GlobalObject.REDIS_MASTER_EXECUTOR.execute(worker);
	}

	@Override
	public boolean updateRole(final long guildId, final long userId, final int role) throws Exception
	{
		RedisWorker<Boolean> worker = new RedisWorker<Boolean>()
		{
			@Override
			public Boolean execute(Jedis jedis) throws Exception
			{
				GuildUser model = getInfo(guildId, userId);
				if(null == model)
					return false;
				
				///将公会会员信息添加到redis中
				String key = RedisKey.GUILD_USER_INFO_KEY_PREFIX + guildId + "_" + userId;
				model.setRole(role);
				JSONObject json = model.toJson();
				jedis.set(key, json.toString());
				
				///清空公会会员缓存信息
				String cacheKey = RedisKey.CACHE_GUILD_USER_LIST_KEY_PREFIX + guildId;
				resultCacheRedis.deleteCache(cacheKey);
				return true;
			}
		};
		return GlobalObject.REDIS_MASTER_EXECUTOR.execute(worker);
	}

	@Override
	public boolean updateStatus(final long guildId, final long guildScore, final long userId, final int status) throws Exception
	{
		RedisWorker<Boolean> worker = new RedisWorker<Boolean>()
		{
			@Override
			public Boolean execute(Jedis jedis) throws Exception
			{
				GuildUser model = getInfo(guildId, userId);
				if(null == model)
					return false;
				
				///将公会会员信息添加到redis中
				String key = RedisKey.GUILD_USER_INFO_KEY_PREFIX + guildId + "_" + userId;
				Date curtime = new Date();
				model.setStatus(status);
				model.setAuditTime(curtime);
				JSONObject json = model.toJson();
				jedis.set(key, json.toString());
				
				///A.审核通过:
				///	1. 将用户从公会待审核会员列表中删除，添加到公会会员列表中
				///	2. 将公会添加到用户的"我的公会"列表中
				///B. 审核未通过
				///	1. 将用户从公会待审核会员列表中删除
				if(status == GuildUserStatus.NORMAL)
				{
					String unauditedListKey = RedisKey.GUILD_USER_UNAUDITED_LIST_KEY_PREFIX + guildId;
					jedis.srem(unauditedListKey, String.valueOf(userId));
					
					String memberListKey = RedisKey.GUILD_USER_LIST_KEY_PREFIX + guildId;
					jedis.sadd(memberListKey, String.valueOf(userId));
					
					String myListKey = RedisKey.MY_GUILD_LIST_KEY_PREFIX + userId;
					jedis.zadd(myListKey, guildScore, String.valueOf(guildId));
				}
				else if(status == GuildUserStatus.AUDIT_NOT_PASS)
				{
					String unauditedListKey = RedisKey.GUILD_USER_UNAUDITED_LIST_KEY_PREFIX + guildId;
					jedis.srem(unauditedListKey, String.valueOf(userId));
				}
				
				///清空公会缓存信息
				String cacheKey = RedisKey.CACHE_GUILD_INFO_KEY_PREFIX + guildId;
				resultCacheRedis.deleteCache(cacheKey);
				
				///清空公会会员缓存信息
				cacheKey = RedisKey.CACHE_GUILD_USER_LIST_KEY_PREFIX + guildId;
				resultCacheRedis.deleteCache(cacheKey);
				
				///清空“我的公会”缓存信息
				cacheKey = RedisKey.CACHE_MY_GUILD_LIST_KEY_PREFIX + userId;
				resultCacheRedis.deleteCache(cacheKey);
				return true;
			}
		};
		return GlobalObject.REDIS_MASTER_EXECUTOR.execute(worker);
	}

	@Override
	public boolean updateLastLoginTime(final long guildId, final long userId, final Date lastLoginTime) throws Exception
	{
		RedisWorker<Boolean> worker = new RedisWorker<Boolean>()
		{
			@Override
			public Boolean execute(Jedis jedis) throws Exception
			{
				GuildUser model = getInfo(guildId, userId);
				if(null == model)
					return false;
				
				///将公会会员信息添加到redis中
				String key = RedisKey.GUILD_USER_INFO_KEY_PREFIX + guildId + "_" + userId;
				model.setLastLoginTime(lastLoginTime);
				JSONObject json = model.toJson();
				jedis.set(key, json.toString());
				return true;
			}
		};
		return GlobalObject.REDIS_MASTER_EXECUTOR.execute(worker);
	}

	@Override
	public GuildUser getInfo(long guildId, long userId) throws Exception
	{
		String key = RedisKey.GUILD_USER_INFO_KEY_PREFIX + guildId + "_" + userId;
		RedisWorker<String> worker = new GetWorker(key);
		String value = GlobalObject.REDIS_SLAVE_EXECUTOR.execute(worker);
		if(StringUtil.isNullOrEmpty(value))
			return null;
		
		JSONObject json = new JSONObject(value);
		return GuildUser.buildByJson(json);
	}

	@Override
	public Set<String> getUserList(final long guildId) throws Exception
	{
		RedisWorker<Set<String>> worker = new RedisWorker<Set<String>>()
		{
			@Override
			public Set<String> execute(Jedis jedis) throws Exception
			{
				String key = RedisKey.GUILD_USER_LIST_KEY_PREFIX + guildId;
				return jedis.smembers(key);
			}
		};
		return GlobalObject.REDIS_SLAVE_EXECUTOR.execute(worker);
	}

	@Override
	public long getUserCount(final long guildId) throws Exception
	{
		RedisWorker<Long> worker = new RedisWorker<Long>()
		{
			@Override
			public Long execute(Jedis jedis) throws Exception
			{
				String key = RedisKey.GUILD_USER_LIST_KEY_PREFIX + guildId;
				Long count = jedis.scard(key);
				return count == null ? 0L : count;
			}
		};
		return GlobalObject.REDIS_SLAVE_EXECUTOR.execute(worker);
	}

	@Override
	public Set<String> getUnauditedUserList(final long guildId) throws Exception
	{
		RedisWorker<Set<String>> worker = new RedisWorker<Set<String>>()
		{
			@Override
			public Set<String> execute(Jedis jedis) throws Exception
			{
				String key = RedisKey.GUILD_USER_UNAUDITED_LIST_KEY_PREFIX + guildId;
				return jedis.smembers(key);
			}
		};
		return GlobalObject.REDIS_SLAVE_EXECUTOR.execute(worker);
	}

	@Override
	public boolean incrNewMemberCount(final long guildId) throws Exception
	{
		RedisWorker<Boolean> worker = new RedisWorker<Boolean>()
		{
			@Override
			public Boolean execute(Jedis jedis) throws Exception
			{
				String key = RedisKey.GUILD_NEW_MEMBER_COUNT_KEY;
				jedis.hincrBy(key, String.valueOf(guildId), 1);
				return true;
			}
		};
		return GlobalObject.REDIS_MASTER_EXECUTOR.execute(worker);
	}

	@Override
	public boolean clearNewMemberCount() throws Exception
	{
		String key = RedisKey.GUILD_NEW_MEMBER_COUNT_KEY;
		RedisWorker<Boolean> worker = new DeleteWorker(key);
		return GlobalObject.REDIS_MASTER_EXECUTOR.execute(worker);
	}

	@Override
	public int getNewMemberCount(final long guildId) throws Exception
	{
		RedisWorker<Integer> worker = new RedisWorker<Integer>()
		{
			@Override
			public Integer execute(Jedis jedis) throws Exception
			{
				String key = RedisKey.GUILD_NEW_MEMBER_COUNT_KEY;
				String count = jedis.hget(key, String.valueOf(guildId));
				if(!StringUtil.isInteger(count))
					return 0;
				return Integer.parseInt(count);
			}
		};
		return GlobalObject.REDIS_SLAVE_EXECUTOR.execute(worker);
	}

	@Override
	public boolean setUnloginMemberCount7Days(final long guildId, final int count) throws Exception
	{
		RedisWorker<Boolean> worker = new RedisWorker<Boolean>()
		{
			@Override
			public Boolean execute(Jedis jedis) throws Exception
			{
				String key = RedisKey.GUILD_UNLOGIN_MEMBER_COUNT_7DAYS_KEY;
				jedis.hset(key, String.valueOf(guildId), String.valueOf(count));
				return true;
			}
		};
		return GlobalObject.REDIS_MASTER_EXECUTOR.execute(worker);
	}

	@Override
	public boolean clearUnloginMemberCount7Days() throws Exception
	{
		String key = RedisKey.GUILD_UNLOGIN_MEMBER_COUNT_7DAYS_KEY;
		RedisWorker<Boolean> worker = new DeleteWorker(key);
		return GlobalObject.REDIS_MASTER_EXECUTOR.execute(worker);
	}

	@Override
	public int getUnloginMemberCount7Days(final long guildId) throws Exception
	{
		RedisWorker<Integer> worker = new RedisWorker<Integer>()
		{
			@Override
			public Integer execute(Jedis jedis) throws Exception
			{
				String key = RedisKey.GUILD_UNLOGIN_MEMBER_COUNT_7DAYS_KEY;
				String count = jedis.hget(key, String.valueOf(guildId));
				if(!StringUtil.isInteger(count))
					return 0;
				return Integer.parseInt(count);
			}
		};
		return GlobalObject.REDIS_SLAVE_EXECUTOR.execute(worker);
	}
	
	@Override
	public boolean setUnloginMemberList7Days(final long guildId, final String memberList) throws Exception
	{
		RedisWorker<Boolean> worker = new RedisWorker<Boolean>()
		{
			@Override
			public Boolean execute(Jedis jedis) throws Exception
			{
				String key = RedisKey.GUILD_UNLOGIN_MEMBER_LIST_7DAYS_KEY;
				jedis.hset(key, String.valueOf(guildId), memberList);
				return true;
			}
		};
		return GlobalObject.REDIS_MASTER_EXECUTOR.execute(worker);
	}
	
	@Override
	public boolean clearUnloginMemberList7Days() throws Exception
	{
	    	String key = RedisKey.GUILD_UNLOGIN_MEMBER_LIST_7DAYS_KEY;
	    	RedisWorker<Boolean> worker = new DeleteWorker(key);
		return GlobalObject.REDIS_MASTER_EXECUTOR.execute(worker);
	}
	
	@Override
	public String getUnloginMemberList7Days(final long guildId) throws Exception
	{
	    	RedisWorker<String> worker = new RedisWorker<String>()
	    	{
	    	    public String execute(Jedis jedis) throws Exception
	    	    {
	    		String key = RedisKey.GUILD_UNLOGIN_MEMBER_LIST_7DAYS_KEY;
	    		return jedis.hget(key, String.valueOf(guildId));
	    	    }
	    	};
	    	return GlobalObject.REDIS_SLAVE_EXECUTOR.execute(worker);
	}

	@Override
	public boolean setUnloginMemberCount30Days(final long guildId, final int count) throws Exception
	{
		RedisWorker<Boolean> worker = new RedisWorker<Boolean>()
		{
			@Override
			public Boolean execute(Jedis jedis) throws Exception
			{
				String key = RedisKey.GUILD_UNLOGIN_MEMBER_COUNT_30DAYS_KEY;
				jedis.hset(key, String.valueOf(guildId), String.valueOf(count));
				return true;
			}
		};
		return GlobalObject.REDIS_MASTER_EXECUTOR.execute(worker);
	}

	@Override
	public boolean clearUnloginMemberCount30Days() throws Exception
	{
		String key = RedisKey.GUILD_UNLOGIN_MEMBER_COUNT_30DAYS_KEY;
		RedisWorker<Boolean> worker = new DeleteWorker(key);
		return GlobalObject.REDIS_MASTER_EXECUTOR.execute(worker);
	}
	
	@Override
	public int getUnloginMemberCount30Days(final long guildId) throws Exception
	{
		RedisWorker<Integer> worker = new RedisWorker<Integer>()
		{
			@Override
			public Integer execute(Jedis jedis) throws Exception
			{
				String key = RedisKey.GUILD_UNLOGIN_MEMBER_COUNT_30DAYS_KEY;
				String count = jedis.hget(key, String.valueOf(guildId));
				if(!StringUtil.isInteger(count))
					return 0;
				return Integer.parseInt(count);
			}
		};
		return GlobalObject.REDIS_SLAVE_EXECUTOR.execute(worker);
	}
	
	@Override
	public boolean setUnloginMemberList30Days(final long guildId, final String memberList) throws Exception
	{
		RedisWorker<Boolean> worker = new RedisWorker<Boolean>()
		{
			@Override
			public Boolean execute(Jedis jedis) throws Exception
			{
				String key = RedisKey.GUILD_UNLOGIN_MEMBER_LIST_30DAYS_KEY;
				jedis.hset(key, String.valueOf(guildId), memberList);
				return true;
			}
		};
		return GlobalObject.REDIS_MASTER_EXECUTOR.execute(worker);
	}
	
	@Override
	public boolean clearUnloginMemberList30Days() throws Exception
	{
	    	String key = RedisKey.GUILD_UNLOGIN_MEMBER_LIST_30DAYS_KEY;
	    	RedisWorker<Boolean> worker = new DeleteWorker(key);
		return GlobalObject.REDIS_MASTER_EXECUTOR.execute(worker);
	}
	
	@Override
	public String getUnloginMemberList30Days(final long guildId) throws Exception
	{
	    	RedisWorker<String> worker = new RedisWorker<String>()
	    	{
	    	    public String execute(Jedis jedis) throws Exception
	    	    {
	    		String key = RedisKey.GUILD_UNLOGIN_MEMBER_LIST_30DAYS_KEY;
	    		return jedis.hget(key, String.valueOf(guildId));
	    	    }
	    	};
	    	return GlobalObject.REDIS_SLAVE_EXECUTOR.execute(worker);
	}
	
	@Override
	public boolean saveUserLastQuitGuild(final long userId) throws Exception
	{
	    RedisWorker<Boolean> worker = new RedisWorker<Boolean>()
	    {
			@Override
        		public Boolean execute(Jedis jedis) throws Exception
        		{
			    String key = RedisKey.GUILD_USER_QUIT_TIME_LIST_KEY;
			    long quitTime = System.currentTimeMillis();
			    jedis.hset(key, userId + "", quitTime + "");
			    return true;
        		}
	    };
	    return GlobalObject.REDIS_MASTER_EXECUTOR.execute(worker);
	}
	
	@Override
	public long getUserLastQuitGuild(final long userId) throws Exception
	{
	    RedisWorker<Long> worker = new RedisWorker<Long>()
	    {
			@Override
        		public Long execute(Jedis jedis) throws Exception
        		{
        		    String key = RedisKey.GUILD_USER_QUIT_TIME_LIST_KEY;
        		    if (jedis.hexists(key, userId + "")) {
        			long quitTime = Long.parseLong(jedis.hget(key, userId + ""));
        			return quitTime;
        		    } 
        		    return 0L;
        		}
	    };
	    return GlobalObject.REDIS_SLAVE_EXECUTOR.execute(worker);
	}
	
	public boolean saveGuildUserInfoList(final long guildId, final String value) throws Exception 
	{
	    RedisWorker<Boolean> worker = new RedisWorker<Boolean>()
	    {
			@Override
			public Boolean execute(Jedis jedis) throws Exception 
			{
			    String key = RedisKey.GUILD_USER_INFO_LIST_KEY_PREFIX + guildId;
			    jedis.set(key, value);
			    return true;
			}
	    };
	    return GlobalObject.REDIS_MASTER_EXECUTOR.execute(worker);
	}
	
	public boolean deleteGuildUserInfoList(long guildId) throws Exception
	{
	    String key = RedisKey.GUILD_USER_INFO_LIST_KEY_PREFIX + guildId;
	    RedisWorker<Boolean> worker = new DeleteWorker(key);
	    return GlobalObject.REDIS_MASTER_EXECUTOR.execute(worker);
	}
	
	public String getGuildUserInfoList(long guildId) throws Exception
	{
	    String key = RedisKey.GUILD_USER_INFO_LIST_KEY_PREFIX + guildId;
	    RedisWorker<String> worker = new GetWorker(key);
	    return GlobalObject.REDIS_SLAVE_EXECUTOR.execute(worker);
	}
	
}