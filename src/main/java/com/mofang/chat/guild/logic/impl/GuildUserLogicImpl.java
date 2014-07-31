package com.mofang.chat.guild.logic.impl;

import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mofang.chat.guild.global.GlobalConfig;
import com.mofang.chat.guild.global.ResultValue;
import com.mofang.chat.guild.global.ReturnCode;
import com.mofang.chat.guild.global.common.GuildUserAuditType;
import com.mofang.chat.guild.global.common.GuildUserRole;
import com.mofang.chat.guild.global.common.GuildUserStatus;
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
	
	private GuildUserLogicImpl()
	{}
	
	public static GuildUserLogicImpl getInstance()
	{
		return LOGIC;
	}

	/**
	 * 申请加入公会
	 * 1. 加入公会成员中(状态: 待审核)
	 */
	@Override
	public ResultValue join(HttpRequestContext context) throws Exception
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
			String postscript = json.optString("postscript", "");
			if(0L == guildId)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
				result.setMessage("公会ID无效");
				return result;
			}
			
			Guild guild = guildRedis.getInfo(guildId);
			if(null == guild)
			{
				result.setCode(ReturnCode.GUILD_NOT_EXISTS);
				result.setMessage("公会不存在");
				return result;
			}
			
			boolean existsUnaudit = guildUserRedis.existsUnAudit(guildId, userId);
			if(existsUnaudit)
			{
				result.setCode(ReturnCode.GUILD_UNAUDIT_MEMBER_EXISTS);
				result.setMessage("您已提交入会申请, 请耐心等待");
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
					result.setMessage("您加入的公会数已到达上限");
					return result;
				}
				
				///判断公会的成员数是否已到达上限
				long userCount = guildUserRedis.getUserCount(guildId);
				if(userCount >= GlobalConfig.MAX_GUILD_MEMBER_COUNT)
				{
					result.setCode(ReturnCode.GUILD_MEMBER_FULL);
					result.setMessage("您所加入的公会已到达人数上限");
					return result;
				}
				
				///判断用户是否已经存在该公会中
				boolean exists = guildUserRedis.exists(guildId, userId);
				if(exists)
				{
					result.setCode(ReturnCode.GUILD_MEMBER_EXISTS);
					result.setMessage("您已加入该公会");
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
			guildUserService.join(model);
			
			///返回结果
			result.setCode(ReturnCode.SUCCESS);
			result.setMessage("OK");
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
			if(0L == guildId)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
				result.setMessage("公会ID无效");
				return result;
			}
			
			Guild guild = guildRedis.getInfo(guildId);
			if(null == guild)
			{
				result.setCode(ReturnCode.GUILD_NOT_EXISTS);
				result.setMessage("公会不存在");
				return result;
			}
			
			if(userId == guild.getCreatorId())
			{
				result.setCode(ReturnCode.GUILD_NOT_EXISTS);
				result.setMessage("会长不能退出公会");
				return result;
			}
			
			///异步执行删除公会成员操作(包括用户主动退出公会以及被管理员踢出公会)
			guildUserService.delete(guildId, userId, 1);
			
			///返回结果
			result.setCode(ReturnCode.SUCCESS);
			result.setMessage("OK");
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
			long applyUid = json.optLong("apply_uid", 0L);
			int auditType = json.optInt("audit_type", 0);
			if(0L == guildId)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
				result.setMessage("公会ID无效");
				return result;
			}
			if(0L == applyUid)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
				result.setMessage("申请用户ID无效");
				return result;
			}
			if(0L == auditType 
			   || (auditType != GuildUserAuditType.AUDIT_PASS 
			   && auditType != GuildUserAuditType.AUDIT_NOT_PASS))
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
				result.setMessage("无效的操作");
				return result;
			}
			
			Guild guild = guildRedis.getInfo(guildId);
			if(null == guild)
			{
				result.setCode(ReturnCode.GUILD_NOT_EXISTS);
				result.setMessage("公会不存在");
				return result;
			}
			
			///如果不是会长
			if(userId != guild.getCreatorId())
			{
				boolean hasPrivilege = hasAdminPrivilege(guildId, userId);
				if(!hasPrivilege)
				{
					result.setCode(ReturnCode.NO_PRIVILEGE_TO_OPERATE);
					result.setMessage("您无权进行审核操作");
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
						result.setMessage("该用户加入的公会数已到达上限");
						return result;
					}
					
					///判断公会的成员数是否已到达上限
					long userCount = guildUserRedis.getUserCount(guildId);
					if(userCount >= GlobalConfig.MAX_GUILD_MEMBER_COUNT)
					{
						result.setCode(ReturnCode.GUILD_MEMBER_FULL);
						result.setMessage("您的公会已到达人数上限");
						return result;
					}
					
					///判断用户是否已经存在该公会中
					boolean exists = guildUserRedis.exists(guildId, applyUid);
					if(exists)
					{
						result.setCode(ReturnCode.GUILD_MEMBER_EXISTS);
						result.setMessage("该用户已加入该公会");
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
			result.setMessage("OK");
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
			long memberUid = json.optLong("member_uid", 0L);
			if(0L == guildId)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
				result.setMessage("公会ID无效");
				return result;
			}
			if(0L == memberUid)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
				result.setMessage("用户ID无效");
				return result;
			}
			
			Guild guild = guildRedis.getInfo(guildId);
			if(null == guild)
			{
				result.setCode(ReturnCode.GUILD_NOT_EXISTS);
				result.setMessage("公会不存在");
				return result;
			}
			
			///如果不是会长
			if(userId != guild.getCreatorId())
			{
				boolean hasPrivilege = hasAdminPrivilege(guildId, userId);
				if(!hasPrivilege)
				{
					result.setCode(ReturnCode.NO_PRIVILEGE_TO_OPERATE);
					result.setMessage("您无权删除会员");
					return result;
				}
				else   ///管理员不能删除管理员
				{
					boolean isAdmin = hasAdminPrivilege(guildId, memberUid);
					if(isAdmin)
					{
						result.setCode(ReturnCode.NO_PRIVILEGE_TO_OPERATE);
						result.setMessage("您无权删除公会管理员");
						return result;
					}
				}
			}
			
			///异步执行删除公会成员操作(包括用户主动退出公会以及被管理员踢出公会)
			guildUserService.delete(guildId, memberUid, 2);
			
			///返回结果
			result.setCode(ReturnCode.SUCCESS);
			result.setMessage("OK");
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
			long memberUid = json.optLong("member_uid", 0L);
			int role = json.optInt("role", 0);
			if(0L == guildId)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
				result.setMessage("公会ID无效");
				return result;
			}
			if(0L == memberUid)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
				result.setMessage("用户ID无效");
				return result;
			}
			if(0 == role)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
				result.setMessage("角色无效");
				return result;
			}
			
			Guild guild = guildRedis.getInfo(guildId);
			if(null == guild)
			{
				result.setCode(ReturnCode.GUILD_NOT_EXISTS);
				result.setMessage("公会不存在");
				return result;
			}
			
			///如果不是会长
			if(userId != guild.getCreatorId())
			{
				result.setCode(ReturnCode.NO_PRIVILEGE_TO_OPERATE);
				result.setMessage("您无权进行角色变更操作");
				return result;
			}
			
			if(memberUid == guild.getCreatorId())
			{
				result.setCode(ReturnCode.NO_PRIVILEGE_TO_OPERATE);
				result.setMessage("您无权进行角色变更操作");
				return result;
			}
			
			///更新用户角色
			guildUserService.changeRole(guildId, memberUid, role);
			
			///返回结果
			result.setCode(ReturnCode.SUCCESS);
			result.setMessage("OK");
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
				result.setMessage("公会ID无效");
				return result;
			}
			long guildId = Long.parseLong(strGuildId);
			///获取公会成员列表(待审核 + 成员列表)
			JSONArray data = guildUserService.getAllUserList(guildId);
			
			///返回结果
			result.setCode(ReturnCode.SUCCESS);
			result.setMessage("OK");
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
}