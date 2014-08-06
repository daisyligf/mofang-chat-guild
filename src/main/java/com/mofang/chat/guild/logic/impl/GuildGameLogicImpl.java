package com.mofang.chat.guild.logic.impl;

import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mofang.chat.guild.global.GlobalConfig;
import com.mofang.chat.guild.global.ResultValue;
import com.mofang.chat.guild.global.ReturnCode;
import com.mofang.chat.guild.logic.GuildGameLogic;
import com.mofang.chat.guild.model.Guild;
import com.mofang.chat.guild.redis.GuildGameRedis;
import com.mofang.chat.guild.redis.GuildRedis;
import com.mofang.chat.guild.redis.impl.GuildGameRedisImpl;
import com.mofang.chat.guild.redis.impl.GuildRedisImpl;
import com.mofang.chat.guild.service.GuildGameService;
import com.mofang.chat.guild.service.impl.GuildGameServiceImpl;
import com.mofang.framework.util.StringUtil;
import com.mofang.framework.web.server.reactor.context.HttpRequestContext;

/**
 * 
 * @author zhaodx
 *
 */
public class GuildGameLogicImpl implements GuildGameLogic
{
	private final static GuildGameLogicImpl LOGIC = new GuildGameLogicImpl();
	private GuildRedis guildRedis = GuildRedisImpl.getInstance();
	private GuildGameRedis guildGameRedis = GuildGameRedisImpl.getInstance();
	
	private GuildGameService guildGameService = GuildGameServiceImpl.getInstance();
	
	private GuildGameLogicImpl()
	{}
	
	public static GuildGameLogicImpl getInstance()
	{
		return LOGIC;
	}

	@Override
	public ResultValue edit(HttpRequestContext context) throws Exception
	{
		ResultValue result = new ResultValue();
		String uidString = context.getParameters("uid");
		if(!StringUtil.isLong(uidString))
		{
			result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
			result.setMessage("参数无效");
			return result;
		}
		
		String postData = context.getPostData();
		if(StringUtil.isNullOrEmpty(postData))
		{
			result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
			return result;
		}
		
		try
		{
			long userId = Long.parseLong(uidString);
			JSONObject json = new JSONObject(postData);
			long guildId = json.optLong("guild_id", 0L);
			JSONArray addGameIds = json.getJSONArray("add_game_ids");
			JSONArray delGameIds = json.getJSONArray("del_game_ids");
			if(0L == guildId)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
				result.setMessage("公会ID无效");
				return result;
			}
			if(null == addGameIds && null == delGameIds)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
				result.setMessage("游戏数组无效");
				return result;
			}
			
			Guild guild = guildRedis.getInfo(guildId);
			if(null == guild)
			{
				result.setCode(ReturnCode.GUILD_NOT_EXISTS);
				result.setMessage("公会不存在");
				return result;
			}
			
			if(guild.getCreatorId() != userId)
			{
				result.setCode(ReturnCode.NO_PRIVILEGE_TO_OPERATE);
				result.setMessage("您无权修改公会游戏");
				return result;
			}
			
			///判断添加的游戏总数是否超出了公会关联游戏总数上限
			int addCount = addGameIds.length();
			int delCount = delGameIds.length();
			int gameCount = (int)guildGameRedis.getGameCountByGuild(guildId);
			int total = addCount - delCount + gameCount;
			
			Lock lock = new ReentrantLock();
			lock.lock();
			try
			{
        			if(total > GlobalConfig.MAX_GUILD_GAME_REF_COUNT)
        			{
        				result.setCode(ReturnCode.OVER_GUILD_GAME_MAX_COUNT);
        				result.setMessage("超过公会关联游戏最大数");
        				return result;
        			}
			} 
			finally
			{
			    lock.unlock();
			}
			
			///执行异步编辑公会游戏操作
			guildGameService.edit(guildId, guild.getGuildName(), userId, addGameIds, delGameIds);
			
			///返回结果
			result.setCode(ReturnCode.SUCCESS);
			result.setMessage("OK");
			return result;
		}
		catch(Exception e)
		{
			throw new Exception("at GuildGameLogicImpl.edit throw an error.", e);
		}
	}

	@Override
	public ResultValue getGuildList(HttpRequestContext context) throws Exception
	{
		ResultValue result = new ResultValue();
		try
		{
			String strGameId = context.getParameters("game_id");
			String strStart = context.getParameters("start");
			String strSize = context.getParameters("size");
			if(!StringUtil.isInteger(strGameId) || "0".equals(strGameId))
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
				result.setMessage("游戏ID无效");
				return result;
			}
			
			int gameId = Integer.parseInt(strGameId);
			int start = 0;
			int size = 50;
			if(StringUtil.isInteger(strStart))
				start = Integer.parseInt(strStart);
			if(StringUtil.isInteger(strSize))
				size = Integer.parseInt(strSize);
			
			int end = start + size - 1;
			JSONObject data = guildGameService.getGuildList(gameId, start, end);
			
			///返回结果
			result.setCode(ReturnCode.SUCCESS);
			result.setMessage("OK");
			result.setData(data);
			return result;
		}
		catch(Exception e)
		{
			throw new Exception("at GuildGameLogicImpl.getGuildList throw an error.", e);
		}
	}

	@Override
	public ResultValue getGameList(HttpRequestContext context) throws Exception
	{
		ResultValue result = new ResultValue();
		try
		{
			String strGuildId = context.getParameters("guild_id");
			if(!StringUtil.isInteger(strGuildId) || "0".equals(strGuildId))
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
				result.setMessage("公会ID无效");
				return result;
			}
			
			long guildId = Long.parseLong(strGuildId);
			Set<String> gameIds = guildGameRedis.getGameListByGuild(guildId);
			JSONArray data = new JSONArray();
			if(null != gameIds)
			{
				for(String gameId : gameIds)
					data.put(gameId);
			}
			
			///返回结果
			result.setCode(ReturnCode.SUCCESS);
			result.setMessage("OK");
			result.setData(data);
			return result;
		}
		catch(Exception e)
		{
			throw new Exception("at GuildGameLogicImpl.getGameList throw an error.", e);
		}
	}
}