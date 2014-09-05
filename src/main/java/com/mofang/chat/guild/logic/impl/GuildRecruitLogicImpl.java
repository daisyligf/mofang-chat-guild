package com.mofang.chat.guild.logic.impl;

import java.util.Date;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mofang.chat.guild.global.ResultValue;
import com.mofang.chat.guild.global.ReturnCode;
import com.mofang.chat.guild.global.common.GuildRecruitStatus;
import com.mofang.chat.guild.logic.GuildRecruitLogic;
import com.mofang.chat.guild.model.Guild;
import com.mofang.chat.guild.model.GuildRecruit;
import com.mofang.chat.guild.mysql.GuildRecruitDao;
import com.mofang.chat.guild.mysql.impl.GuildRecruitDaoImpl;
import com.mofang.chat.guild.redis.GuildRecruitRedis;
import com.mofang.chat.guild.redis.GuildRedis;
import com.mofang.chat.guild.redis.impl.GuildRecruitRedisImpl;
import com.mofang.chat.guild.redis.impl.GuildRedisImpl;
import com.mofang.chat.guild.service.GuildService;
import com.mofang.chat.guild.service.impl.GuildServiceImpl;
import com.mofang.framework.util.StringUtil;
import com.mofang.framework.web.server.reactor.context.HttpRequestContext;

/**
 * 
 * @author zhaodx
 *
 */
public class GuildRecruitLogicImpl implements GuildRecruitLogic
{
	private final static GuildRecruitLogicImpl LOGIC = new GuildRecruitLogicImpl();
	private GuildRedis guildRedis = GuildRedisImpl.getInstance();
	private GuildRecruitRedis guildRecruitRedis = GuildRecruitRedisImpl.getInstance();
	private GuildRecruitDao guildRecruitDao = GuildRecruitDaoImpl.getInstance();
	private GuildService guildService = GuildServiceImpl.getInstance();
	
	private GuildRecruitLogicImpl()
	{}
	
	public static GuildRecruitLogicImpl getInstance()
	{
		return LOGIC;
	}

	@Override
	public ResultValue publish(HttpRequestContext context) throws Exception
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
			String content = json.optString("content", "");
			if(0L == guildId)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
				result.setMessage("公会ID无效");
				return result;
			}
			if(StringUtil.isNullOrEmpty(content))
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
				result.setMessage("招募信息不能为空");
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
				result.setMessage("您无权发布招募信息");
				return result;
			}
			
			///构建公会招募信息
			GuildRecruit model = new GuildRecruit();
			model.setGuildId(guildId);
			model.setContent(content);
			model.setStatus(GuildRecruitStatus.UNAUDITED);
			Date curDate = new Date();
			model.setApplyTime(curDate);
			model.setAuditTime(curDate);
			
			///保存公会招募信息
			guildRecruitDao.add(model);
			
			///返回结果
			result.setCode(ReturnCode.SUCCESS);
			result.setMessage("OK");
			return result;
		}
		catch(Exception e)
		{
			throw new Exception("at GuildRecruitLogicImpl.publish throw an error.", e);
		}
	}

	@Override
	public ResultValue audit(HttpRequestContext context) throws Exception
	{
		ResultValue result = new ResultValue();
		String postData = context.getPostData();
		if(StringUtil.isNullOrEmpty(postData))
		{
			result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
			return result;
		}
		
		try
		{
			JSONObject json = new JSONObject(postData);
			int auditType = json.optInt("audit_type", 0);
			int guildId = json.optInt("guild_id", 0);
			int recruitId = json.optInt("recruit_id", 0);
			if(0L == guildId)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
				result.setMessage("公会ID无效");
				return result;
			}
			
			if (0L == recruitId)
			{
			    	result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
				result.setMessage("招募信息ID无效");
				return result;
			}
			
			///更新公会招募信息状态
			guildRecruitDao.updateStatus(recruitId, auditType);
			if(auditType == GuildRecruitStatus.NORMAL)
			{
				guildRecruitRedis.addAuditList(guildId, System.currentTimeMillis());
			}
			
			///返回结果
			result.setCode(ReturnCode.SUCCESS);
			result.setMessage("OK");
			return result;
		}
		catch(Exception e)
		{
			throw new Exception("at GuildRecruitLogicImpl.audit throw an error.", e);
		}
	}

	@Override
	public ResultValue getRecruitList(HttpRequestContext context) throws Exception
	{
		ResultValue result = new ResultValue();
		try
		{
			JSONArray  data = new JSONArray();
			Set<String> guildIds = guildRecruitRedis.getAuditList();
			if(null != guildIds)
			{
				long guildId = 0L;
				JSONObject guildJson = null;
				for(String guildIdStr : guildIds)
				{
					guildId = Long.parseLong(guildIdStr);
					guildJson = guildService.getInfo(guildId);
					if(null == guildJson)
						continue;
					
					guildJson.remove("group");
					guildJson.remove("games");
					data.put(guildJson);
				}
			}
			
			///返回结果
			result.setCode(ReturnCode.SUCCESS);
			result.setMessage("OK");
			result.setData(data);
			return result;
		}
		catch(Exception e)
		{
			throw new Exception("at GuildRecruitLogicImpl.getRecruitList throw an error.", e);
		}
	}
}