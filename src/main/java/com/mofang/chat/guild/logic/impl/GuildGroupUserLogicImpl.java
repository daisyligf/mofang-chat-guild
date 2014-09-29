package com.mofang.chat.guild.logic.impl;

import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mofang.chat.guild.global.GlobalObject;
import com.mofang.chat.guild.global.ResultValue;
import com.mofang.chat.guild.global.ReturnCode;
import com.mofang.chat.guild.logic.GuildGroupUserLogic;
import com.mofang.chat.guild.model.GuildGroup;
import com.mofang.chat.guild.model.GuildGroupUser;
import com.mofang.chat.guild.mysql.GuildGroupUserDao;
import com.mofang.chat.guild.mysql.impl.GuildGroupUserDaoImpl;
import com.mofang.chat.guild.redis.GuildGroupRedis;
import com.mofang.chat.guild.redis.GuildGroupUserRedis;
import com.mofang.chat.guild.redis.impl.GuildGroupRedisImpl;
import com.mofang.chat.guild.redis.impl.GuildGroupUserRedisImpl;
import com.mofang.chat.guild.service.GuildGroupUserService;
import com.mofang.chat.guild.service.impl.GuildGroupUserServiceImpl;
import com.mofang.framework.util.StringUtil;
import com.mofang.framework.web.server.reactor.context.HttpRequestContext;

/**
 * 
 * @author zhaodx
 *
 */
public class GuildGroupUserLogicImpl implements GuildGroupUserLogic
{
	private final static GuildGroupUserLogicImpl LOGIC = new GuildGroupUserLogicImpl();
	private GuildGroupRedis guildGroupRedis = GuildGroupRedisImpl.getInstance();
	private GuildGroupUserRedis guildGroupUserRedis = GuildGroupUserRedisImpl.getInstance();
	private GuildGroupUserDao guildGroupUserDao = GuildGroupUserDaoImpl.getInstance();
	private GuildGroupUserService guildGroupUserService = GuildGroupUserServiceImpl.getInstance();
	
	private GuildGroupUserLogicImpl()
	{}
	
	public static GuildGroupUserLogicImpl getInstance()
	{
		return LOGIC;
	}

	@Override
	public ResultValue join(HttpRequestContext context) throws Exception
	{
		ResultValue result = new ResultValue();
		String uidString = context.getParameters("uid");
		if(!StringUtil.isLong(uidString))
		{
			result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
			result.setMessage(GlobalObject.GLOBAL_MESSAGE.CLIENT_REQUEST_DATA_IS_INVALID);
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
			long groupId = json.optLong("group_id", 0L);
			if(0L == groupId)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
				result.setMessage(GlobalObject.GLOBAL_MESSAGE.GROUP_ID_INVALID);
				return result;
			}
			
			///构建群组用户信息
			GuildGroupUser model = new GuildGroupUser();
			model.setGroupId(groupId);
			model.setUserId(userId);
			model.setReceiveNotify(1);
			model.setCreateTime(new Date());
			guildGroupUserRedis.add(model);
			guildGroupUserDao.add(model);
			
			///返回结果
			result.setCode(ReturnCode.SUCCESS);
			result.setMessage(GlobalObject.GLOBAL_MESSAGE.SUCCESS);
			return result;
		}
		catch(Exception e)
		{
			throw new Exception("at GuildGroupUserLogicImpl.join throw an error.", e);
		}
	}

	@Override
	public ResultValue quit(HttpRequestContext context) throws Exception
	{
		ResultValue result = new ResultValue();
		String uidString = context.getParameters("uid");
		if(!StringUtil.isLong(uidString))
		{
			result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
			result.setMessage(GlobalObject.GLOBAL_MESSAGE.CLIENT_REQUEST_DATA_IS_INVALID);
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
			long groupId = json.optLong("group_id", 0L);
			if(0L == groupId)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
				result.setMessage(GlobalObject.GLOBAL_MESSAGE.GROUP_ID_INVALID);
				return result;
			}
			
			///从群组中删除用户
			guildGroupUserRedis.delete(groupId, userId);
			guildGroupUserDao.delete(groupId, userId);
			
			///返回结果
			result.setCode(ReturnCode.SUCCESS);
			result.setMessage(GlobalObject.GLOBAL_MESSAGE.SUCCESS);
			return result;
		}
		catch(Exception e)
		{
			throw new Exception("at GuildGroupUserLogicImpl.quit throw an error.", e);
		}
	}

	@Override
	public ResultValue updateReceiveNotify(HttpRequestContext context) throws Exception
	{
		ResultValue result = new ResultValue();
		String uidString = context.getParameters("uid");
		if(!StringUtil.isLong(uidString))
		{
			result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
			result.setMessage(GlobalObject.GLOBAL_MESSAGE.CLIENT_REQUEST_DATA_IS_INVALID);
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
			long groupId = json.optLong("group_id", 0L);
			int receiveNotify = json.optInt("receive_notify", 0);
			if(0L == groupId)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
				result.setMessage(GlobalObject.GLOBAL_MESSAGE.GROUP_ID_INVALID);
				return result;
			}
			
			///更新用户是否接收群组消息通知
		    guildGroupUserRedis.updateReceiveNotify(groupId, userId, receiveNotify);
		    guildGroupUserDao.updateReceiveNotify(groupId, userId, receiveNotify);
			
			///返回结果
			result.setCode(ReturnCode.SUCCESS);
			result.setMessage(GlobalObject.GLOBAL_MESSAGE.SUCCESS);
			return result;
		}
		catch(Exception e)
		{
			throw new Exception("at GuildGroupUserLogicImpl.quit throw an error.", e);
		}
	}

	@Override
	public ResultValue getReceiveNotify(HttpRequestContext context) throws Exception
	{
		ResultValue result = new ResultValue();
		String uidString = context.getParameters("uid");
		if(!StringUtil.isLong(uidString))
		{
			result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
			result.setMessage(GlobalObject.GLOBAL_MESSAGE.CLIENT_REQUEST_DATA_IS_INVALID);
			return result;
		}
		try
		{
			long userId = Long.parseLong(uidString);
			String groupIds = context.getParameters("group_ids");
			if(StringUtil.isNullOrEmpty(groupIds))
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
				result.setMessage(GlobalObject.GLOBAL_MESSAGE.GROUP_ID_INVALID);
				return result;
			}
			
			String[] ids = groupIds.split(",");
			JSONArray data = new JSONArray();
			JSONObject item = null;
			long groupId = 0L;
			String receiveNotify;
			for(String strGroupId : ids)
			{
				groupId = Long.parseLong(strGroupId);
				receiveNotify = guildGroupUserRedis.getRecevieNotify(groupId, userId);
				if(StringUtil.isNullOrEmpty(receiveNotify))
					receiveNotify = "1";
				item = new JSONObject();
				item.put("group_id", groupId);
				item.put("recevie_notify", Integer.parseInt(receiveNotify));
				data.put(item);
			}
			
			///返回结果
			result.setCode(ReturnCode.SUCCESS);
			result.setMessage(GlobalObject.GLOBAL_MESSAGE.SUCCESS);
			result.setData(data);
			return result;
		}
		catch(Exception e)
		{
			throw new Exception("at GuildGroupUserLogicImpl.quit throw an error.", e);
		}
	}

	@Override
	public ResultValue delete(HttpRequestContext context) throws Exception
	{
		ResultValue result = new ResultValue();
		String uidString = context.getParameters("uid");
		if(!StringUtil.isLong(uidString))
		{
			result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
			result.setMessage(GlobalObject.GLOBAL_MESSAGE.CLIENT_REQUEST_DATA_IS_INVALID);
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
			long groupId = json.optLong("group_id", 0L);
			long memberUid = json.optLong("member_uid", 0L);
			if(0L == groupId)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
				result.setMessage(GlobalObject.GLOBAL_MESSAGE.GROUP_ID_INVALID);
				return result;
			}
			if(0L == memberUid)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
				result.setMessage(GlobalObject.GLOBAL_MESSAGE.USER_ID_INVALID);
				return result;
			}
			
			GuildGroup guildGroup = guildGroupRedis.getInfo(groupId);
			if(null == guildGroup)
			{
				result.setCode(ReturnCode.GUILD_GROUP_NOT_EXISTS);
				result.setMessage(GlobalObject.GLOBAL_MESSAGE.GUILD_GROUP_NOT_EXISTS);
				return result;
			}
			
			if(userId != guildGroup.getCreatorId())
			{
				result.setCode(ReturnCode.NO_PRIVILEGE_TO_OPERATE);
				result.setMessage(GlobalObject.GLOBAL_MESSAGE.NO_PRIVILEGE_TO_DEL_GROUP_USER);
				return result;
			}
			
			///从群组中删除用户
			guildGroupUserRedis.delete(groupId, memberUid);
			guildGroupUserDao.delete(groupId, memberUid);
			
			///返回结果
			result.setCode(ReturnCode.SUCCESS);
			result.setMessage(GlobalObject.GLOBAL_MESSAGE.SUCCESS);
			return result;
		}
		catch(Exception e)
		{
			throw new Exception("at GuildGroupUserLogicImpl.delete throw an error.", e);
		}
	}

	@Override
	public ResultValue getList(HttpRequestContext context) throws Exception
	{
		ResultValue result = new ResultValue();
		try
		{
			String strGroupId = context.getParameters("group_id");
			if(!StringUtil.isLong(strGroupId) || "0".equals(strGroupId))
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
				result.setMessage(GlobalObject.GLOBAL_MESSAGE.GROUP_ID_INVALID);
				return result;
			}
			long groupId = Long.parseLong(strGroupId);
			///获取群组成员列表
			JSONArray data = guildGroupUserService.getUserList(groupId);
			
			///返回结果
			result.setCode(ReturnCode.SUCCESS);
			result.setMessage(GlobalObject.GLOBAL_MESSAGE.SUCCESS);
			result.setData(data);
			return result;
		}
		catch(Exception e)
		{
			throw new Exception("at GuildGroupUserLogicImpl.getList throw an error.", e);
		}
	}
}