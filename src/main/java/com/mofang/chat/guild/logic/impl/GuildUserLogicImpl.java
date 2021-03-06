package com.mofang.chat.guild.logic.impl;

import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mofang.chat.guild.component.TaskExecComponent;
import com.mofang.chat.guild.global.GlobalConfig;
import com.mofang.chat.guild.global.GlobalObject;
import com.mofang.chat.guild.global.ResultValue;
import com.mofang.chat.guild.global.ReturnCode;
import com.mofang.chat.guild.global.common.GuildUserAuditType;
import com.mofang.chat.guild.global.common.GuildUserRole;
import com.mofang.chat.guild.global.common.GuildUserStatus;
import com.mofang.chat.guild.global.common.GuildUserUnloginType;
import com.mofang.chat.guild.global.common.TaskExecCode;
import com.mofang.chat.guild.logic.GuildUserLogic;
import com.mofang.chat.guild.model.Guild;
import com.mofang.chat.guild.model.GuildUser;
import com.mofang.chat.guild.mysql.GuildUserDao;
import com.mofang.chat.guild.mysql.impl.GuildUserDaoImpl;
import com.mofang.chat.guild.redis.GuildRedis;
import com.mofang.chat.guild.redis.GuildUserRedis;
import com.mofang.chat.guild.redis.impl.GuildRedisImpl;
import com.mofang.chat.guild.redis.impl.GuildUserRedisImpl;
import com.mofang.chat.guild.service.GuildUserService;
import com.mofang.chat.guild.service.impl.GuildUserServiceImpl;
import com.mofang.framework.util.StringUtil;
import com.mofang.framework.web.server.reactor.context.HttpRequestContext;

/**
 * 
 * @author zhaodx
 *
 */
public class GuildUserLogicImpl implements GuildUserLogic
{
	private final static GuildUserLogicImpl LOGIC = new GuildUserLogicImpl();
	private GuildRedis guildRedis = GuildRedisImpl.getInstance();
	private GuildUserRedis guildUserRedis = GuildUserRedisImpl.getInstance();
	private GuildUserDao guildUserDao = GuildUserDaoImpl.getInstance();
	private GuildUserService guildUserService = GuildUserServiceImpl.getInstance();
	private TaskExecComponent taskExecComponent = TaskExecComponent.getInstance();
	private final static long minQuitJoinGuildTime = GlobalConfig.MIN_QUIT_JOIN_GUILD_HOURS * 60 * 60 * 1000;
	
	private GuildUserLogicImpl()
	{}
	
	public static GuildUserLogicImpl getInstance()
	{
		return LOGIC;
	}

	/**
	 * 申请加入公会
	 * 1. 加入公会成员中(状态: 待审核)
	 * 修改
	 */
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
			result.setMessage(GlobalObject.GLOBAL_MESSAGE.CLIENT_REQUEST_DATA_IS_INVALID);
			return result;
		}
		
		try
		{
			long userId = Long.parseLong(uidString);
			JSONObject json = new JSONObject(postData);
			long guildId = json.optLong("guild_id", 0L);
			String postscript = json.optString("postscript", "");
			if (0L == guildId)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
				result.setMessage(GlobalObject.GLOBAL_MESSAGE.GUILD_ID_INVALID);
				return result;
			}
			
			long lastQuitTime = guildUserRedis.getUserLastQuitGuild(userId);
			if (System.currentTimeMillis() - lastQuitTime < minQuitJoinGuildTime)
			{
    			    	result.setCode(ReturnCode.USER_QUIT_JOIN_GUILD_LIMIT);
    			    	result.setMessage(GlobalObject.GLOBAL_MESSAGE.USER_QUIT_JOIN_GUILD_LIMIT);
    			    	return result;
			}
			
			Guild guild = guildRedis.getInfo(guildId);
			if(null == guild)
			{
				result.setCode(ReturnCode.GUILD_NOT_EXISTS);
				result.setMessage(GlobalObject.GLOBAL_MESSAGE.GUILD_NOT_EXISTS);
				return result;
			}
			
			boolean existsUnaudit = guildUserRedis.existsUnAudit(guildId, userId);
			if(existsUnaudit)
			{
				result.setCode(ReturnCode.GUILD_UNAUDIT_MEMBER_EXISTS);
				result.setMessage(GlobalObject.GLOBAL_MESSAGE.GUILD_UNAUDIT_MEMBER_EXISTS);
				return result;
			}

			Lock lock = new ReentrantLock();
			lock.lock();
			try
			{
				///判断用户加入的公会数是否已到达上限
				long joinCount = guildUserDao.getJoinCount(userId);
				if(joinCount >= GlobalConfig.MAX_JOIN_GUILD_COUNT)
				{
					result.setCode(ReturnCode.USER_JOIN_GUILD_UPPER_LIMIT);
					result.setMessage(GlobalObject.GLOBAL_MESSAGE.USER_JOIN_GUILD_UPPER_LIMIT);
					return result;
				}
				
				///判断公会的成员数是否已到达上限
				long userCount = guildUserRedis.getUserCount(guildId);
				if(userCount >= GlobalConfig.MAX_GUILD_MEMBER_COUNT)
				{
					result.setCode(ReturnCode.GUILD_MEMBER_FULL);
					result.setMessage(GlobalObject.GLOBAL_MESSAGE.GUILD_MEMBER_FULL);
					return result;
				}
				
				///判断用户是否已经存在该公会中
				boolean exists = guildUserRedis.exists(guildId, userId);
				if(exists)
				{
					result.setCode(ReturnCode.GUILD_MEMBER_EXISTS);
					result.setMessage(GlobalObject.GLOBAL_MESSAGE.GUILD_MEMBER_EXISTS);
					return result;
				}
			}
			finally
			{
				lock.unlock();
			}
			
			///构建公会成员信息
			GuildUser model = new GuildUser();
			model.setGuildId(guildId);
			model.setUserId(userId);
			model.setPostscript(postscript);
			model.setRole(GuildUserRole.MEMBER);
			model.setStatus(GuildUserStatus.UNAUDITED);
			Date curdate = new Date();
			model.setApplyTime(curdate);
			model.setAuditTime(curdate);
			model.setLastLoginTime(curdate);
			
			///异步执行申请加入公会操作
			guildUserService.join(model, guild);
			
			if (guild.getNeedAudit() == 0) 
			{
				guildUserService.audit(guild, userId, GuildUserAuditType.AUDIT_PASS);	
			}
			
			///异步执行任务接口
			taskExecComponent.exec(userId, TaskExecCode.JOIN_GUILD);
			///返回结果
			result.setCode(ReturnCode.SUCCESS);
			result.setMessage(GlobalObject.GLOBAL_MESSAGE.SUCCESS);
			return result;
		}
		catch(Exception e)
		{
			throw new Exception("at GuildUserLogicImpl.join throw an error.", e);
		}
	}

	/**
	 * 退出公会
	 * 1. 从公会成员中删除
	 * 2. 从公会默认群组成员中删除
	 * 3. 从公会游戏群组成员中删除
	 * 4. 将公会从用户的“我的公会”中删除
	 */
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
			long guildId = json.optLong("guild_id", 0L);
			
			if (0L == guildId)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
				result.setMessage(GlobalObject.GLOBAL_MESSAGE.GUILD_ID_INVALID);
				return result;
			}
			
			Guild guild = guildRedis.getInfo(guildId);
			if (null == guild)
			{
				result.setCode(ReturnCode.GUILD_NOT_EXISTS);
				result.setMessage(GlobalObject.GLOBAL_MESSAGE.GUILD_NOT_EXISTS);
				return result;
			}
			
			if (userId == guild.getCreatorId())
			{
				result.setCode(ReturnCode.GUILD_CREATOR_CAN_NOT_QUIT);
				result.setMessage(GlobalObject.GLOBAL_MESSAGE.GUILD_CREATOR_CAN_NOT_QUIT);
				return result;
			}
			
			///异步执行删除公会成员操作(包括用户主动退出公会以及被管理员踢出公会)
			guildUserService.delete(guildId, userId, 1);
			// 保存用户退出时间
			guildUserRedis.saveUserLastQuitGuild(userId);
			
			///返回结果
			result.setCode(ReturnCode.SUCCESS);
			result.setMessage(GlobalObject.GLOBAL_MESSAGE.SUCCESS);
			return result;
		}
		catch(Exception e)
		{
			throw new Exception("at GuildUserLogicImpl.quit throw an error.", e);
		}
	}

	/**
	 * 审核申请加入公会的成员
	 * A. 审核通过:
	 * 		1. 更新成员的状态(审核通过)
	 * 		2. 将用户添加到公会默认群组
	 * 		3. 将公会添加到用户的“我的公会”中
	 * B. 审核未通过:
	 * 		1. 更新成员的状体(审核未通过)
	 * 		2. 从公会成员列表中删除
	 */
	@Override
	public ResultValue audit(HttpRequestContext context) throws Exception
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
			long guildId = json.optLong("guild_id", 0L);
			long applyUid = json.optLong("apply_uid", 0L);
			int auditType = json.optInt("audit_type", 0);
			if(0L == guildId)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
				result.setMessage(GlobalObject.GLOBAL_MESSAGE.GUILD_ID_INVALID);
				return result;
			}
			if(0L == applyUid)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
				result.setMessage(GlobalObject.GLOBAL_MESSAGE.APPLY_UID_INVALID);
				return result;
			}
			if(0L == auditType 
			   || (auditType != GuildUserAuditType.AUDIT_PASS 
			   && auditType != GuildUserAuditType.AUDIT_NOT_PASS))
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
				result.setMessage(GlobalObject.GLOBAL_MESSAGE.OP_TYPE_INVALID);
				return result;
			}
			
			Guild guild = guildRedis.getInfo(guildId);
			if(null == guild)
			{
				result.setCode(ReturnCode.GUILD_NOT_EXISTS);
				result.setMessage(GlobalObject.GLOBAL_MESSAGE.GUILD_NOT_EXISTS);
				return result;
			}
			
			///如果不是会长
			if(userId != guild.getCreatorId())
			{
				boolean hasPrivilege = hasAdminPrivilege(guildId, userId);
				if(!hasPrivilege)
				{
					result.setCode(ReturnCode.NO_PRIVILEGE_TO_OPERATE);
					result.setMessage(GlobalObject.GLOBAL_MESSAGE.NO_PRIVILEGE_TO_OPERATE_AUDIT);
					return result;
				}
			}
			
			if(auditType == GuildUserAuditType.AUDIT_PASS)
			{
				Lock lock = new ReentrantLock();
				lock.lock();
				try
				{
					///判断用户加入的公会数是否已到达上限
					long joinCount = guildUserDao.getJoinCount(applyUid);
					if(joinCount >= GlobalConfig.MAX_JOIN_GUILD_COUNT)
					{
						result.setCode(ReturnCode.USER_JOIN_GUILD_UPPER_LIMIT);
						result.setMessage(GlobalObject.GLOBAL_MESSAGE.HE_JOIN_GUILD_UPPER_LIMIT);
						return result;
					}
					
					///判断公会的成员数是否已到达上限
					long userCount = guildUserRedis.getUserCount(guildId);
					if(userCount >= GlobalConfig.MAX_GUILD_MEMBER_COUNT)
					{
						result.setCode(ReturnCode.GUILD_MEMBER_FULL);
						result.setMessage(GlobalObject.GLOBAL_MESSAGE.GUILD_MEMBER_FULL);
						return result;
					}
					
					///判断用户是否已经存在该公会中
					boolean exists = guildUserRedis.exists(guildId, applyUid);
					if(exists)
					{
						result.setCode(ReturnCode.GUILD_MEMBER_EXISTS);
						result.setMessage(GlobalObject.GLOBAL_MESSAGE.HE_JOINED_GUILD_ALREADY);
						return result;
					}
				}
				finally
				{
					lock.unlock();
				}
			}
			
			///异步审核
			guildUserService.audit(guild, applyUid, auditType);
			
			///返回结果
			result.setCode(ReturnCode.SUCCESS);
			result.setMessage(GlobalObject.GLOBAL_MESSAGE.SUCCESS);
			return result;
		}
		catch(Exception e)
		{
			throw new Exception("at GuildUserLogicImpl.audit throw an error.", e);
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
			long guildId = json.optLong("guild_id", 0L);
			long memberUid = json.optLong("member_uid", 0L);
			if(0L == guildId)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
				result.setMessage(GlobalObject.GLOBAL_MESSAGE.GUILD_ID_INVALID);
				return result;
			}
			if(0L == memberUid)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
				result.setMessage(GlobalObject.GLOBAL_MESSAGE.USER_ID_INVALID);
				return result;
			}
			
			Guild guild = guildRedis.getInfo(guildId);
			if(null == guild)
			{
				result.setCode(ReturnCode.GUILD_NOT_EXISTS);
				result.setMessage(GlobalObject.GLOBAL_MESSAGE.GUILD_NOT_EXISTS);
				return result;
			}
			
			///如果不是会长
			if(userId != guild.getCreatorId())
			{
				boolean hasPrivilege = hasAdminPrivilege(guildId, userId);
				if(!hasPrivilege)
				{
					result.setCode(ReturnCode.NO_PRIVILEGE_TO_OPERATE);
					result.setMessage(GlobalObject.GLOBAL_MESSAGE.NO_PRIVILEGE_TO_OPERATE);
					return result;
				}
				else   ///管理员不能删除管理员
				{
					boolean isAdmin = hasAdminPrivilege(guildId, memberUid);
					if(isAdmin)
					{
						result.setCode(ReturnCode.NO_PRIVILEGE_TO_OPERATE);
						result.setMessage(GlobalObject.GLOBAL_MESSAGE.NO_PRIVILEGE_TO_OPERATE);
						return result;
					}
				}
			}
			
			///异步执行删除公会成员操作(包括用户主动退出公会以及被管理员踢出公会)
			guildUserService.delete(guildId, memberUid, 2);
			
			///返回结果
			result.setCode(ReturnCode.SUCCESS);
			result.setMessage(GlobalObject.GLOBAL_MESSAGE.SUCCESS);
			return result;
		}
		catch(Exception e)
		{
			throw new Exception("at GuildUserLogicImpl.delete throw an error.", e);
		}
	}

	@Override
	public ResultValue changeRole(HttpRequestContext context) throws Exception
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
			long guildId = json.optLong("guild_id", 0L);
			long memberUid = json.optLong("member_uid", 0L);
			int role = json.optInt("role", 0);
			if(0L == guildId)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
				result.setMessage(GlobalObject.GLOBAL_MESSAGE.GUILD_ID_INVALID);
				return result;
			}
			if(0L == memberUid)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
				result.setMessage(GlobalObject.GLOBAL_MESSAGE.USER_ID_INVALID);
				return result;
			}
			if(0 == role)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
				result.setMessage(GlobalObject.GLOBAL_MESSAGE.ROLE_INVALID);
				return result;
			}
			
			Guild guild = guildRedis.getInfo(guildId);
			if(null == guild)
			{
				result.setCode(ReturnCode.GUILD_NOT_EXISTS);
				result.setMessage(GlobalObject.GLOBAL_MESSAGE.GUILD_NOT_EXISTS);
				return result;
			}
			
			///如果不是会长
			if(userId != guild.getCreatorId())
			{
				result.setCode(ReturnCode.NO_PRIVILEGE_TO_OPERATE);
				result.setMessage(GlobalObject.GLOBAL_MESSAGE.NO_PRIVILEGE_TO_CHANGE_ROLE);
				return result;
			}
			
			if(memberUid == guild.getCreatorId())
			{
				result.setCode(ReturnCode.NO_PRIVILEGE_TO_OPERATE);
				result.setMessage(GlobalObject.GLOBAL_MESSAGE.NO_PRIVILEGE_TO_CHANGE_ROLE);
				return result;
			}
			
			///更新用户角色
			guildUserService.changeRole(guildId, memberUid, role);
			
			///返回结果
			result.setCode(ReturnCode.SUCCESS);
			result.setMessage(GlobalObject.GLOBAL_MESSAGE.SUCCESS);
			return result;
		}
		catch(Exception e)
		{
			throw new Exception("at GuildUserLogicImpl.changeRole throw an error.", e);
		}
	}

	@Override
	public ResultValue getList(HttpRequestContext context) throws Exception
	{
		ResultValue result = new ResultValue();
		try
		{
			String strGuildId = context.getParameters("guild_id");
			if(!StringUtil.isLong(strGuildId) || "0".equals(strGuildId))
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
				result.setMessage(GlobalObject.GLOBAL_MESSAGE.GUILD_ID_INVALID);
				return result;
			}
			long guildId = Long.parseLong(strGuildId);
			///获取公会成员列表(待审核 + 成员列表)
			JSONArray data = guildUserService.getAllUserList(guildId);
			
			///返回结果
			result.setCode(ReturnCode.SUCCESS);
			result.setMessage(GlobalObject.GLOBAL_MESSAGE.SUCCESS);
			result.setData(data);
			return result;
		}
		catch(Exception e)
		{
			throw new Exception("at GuildUserLogicImpl.getList throw an error.", e);
		}
	}
	
	private boolean hasAdminPrivilege(long guildId, long userId) throws Exception
	{
		try
		{
			GuildUser guildUser = guildUserRedis.getInfo(guildId, userId);
			if(null == guildUser)
				return false;
			
			if(guildUser.getRole() != GuildUserRole.ADMIN)
				return false;
			return true;
		}
		catch(Exception e)
		{
			throw new Exception("at GuildUserLogicImpl.hasAdminPrivilege throw an error.", e);
		}
	}
	
	public ResultValue userExists(HttpRequestContext context) throws Exception
	{
	    ResultValue result = new ResultValue();
	    try {
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
        		result.setMessage(GlobalObject.GLOBAL_MESSAGE.CLIENT_REQUEST_DATA_IS_INVALID);
        		return result;
        	}
        	JSONObject json = new JSONObject(postData);
        	long guildId = json.optLong("guild_id", 0L);
        	long userId = Long.parseLong(uidString);
        	boolean exists = guildUserRedis.exists(guildId, userId);
        	JSONObject data = new JSONObject();
        	if (exists)
        	{
        	    data.put("exists", 1);
        	} else
        	{
        	    data.put("exists", 0);
        	}
        	result.setCode(ReturnCode.SUCCESS);
        	result.setMessage(GlobalObject.GLOBAL_MESSAGE.SUCCESS);
        	result.setData(data);
        	return result;
	    }
	    catch(Exception e)
	    {
		throw new Exception("at GuildUserLogicImpl.userExists throw an error.", e);
	    }
	}
	
	public ResultValue unloginList(HttpRequestContext context) throws Exception
	{
	    ResultValue result = new ResultValue();
	    try 
	    {
		String strGuildId = context.getParameters("guild_id");
		if(!StringUtil.isLong(strGuildId) || "0".equals(strGuildId))
		{
			result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
			result.setMessage(GlobalObject.GLOBAL_MESSAGE.GUILD_ID_INVALID);
			return result;
		}
		long guildId = Long.parseLong(strGuildId);
		
		String strType = context.getParameters("type");
		if(!StringUtil.isLong(strType) || "0".equals(strType))
		{
			result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
			result.setMessage(GlobalObject.GLOBAL_MESSAGE.TYPE_INVALID);
			return result;
		}
		int type = Integer.parseInt(strType);
		String content = "";
		// 判断请求数据的类型
		if (type == GuildUserUnloginType.SEVEN_DAYS) {
		    content = guildUserRedis.getUnloginMemberList7Days(guildId);
		} else if (type == GuildUserUnloginType.THIRTY_DAYS) {
		    content = guildUserRedis.getUnloginMemberList30Days(guildId);
		}
		
		
		JSONArray memberList = new JSONArray();
		if (content != null)
		{
		    memberList = new JSONArray(content);
		}
		result.setCode(ReturnCode.SUCCESS);
		result.setMessage(GlobalObject.GLOBAL_MESSAGE.SUCCESS);
		result.setData(memberList);
		return result;
	    }
	    catch(Exception e)
	    {
		throw new Exception("at GuildUserLogicImpl.unloginList throw an error.", e);
	    }
	}
}