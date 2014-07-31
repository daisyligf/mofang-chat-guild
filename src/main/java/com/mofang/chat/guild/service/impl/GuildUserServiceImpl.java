package com.mofang.chat.guild.service.impl;

import java.util.Date;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mofang.chat.guild.component.NotifyPushComponent;
import com.mofang.chat.guild.component.UserComponent;
import com.mofang.chat.guild.entity.User;
import com.mofang.chat.guild.global.GlobalConfig;
import com.mofang.chat.guild.global.GlobalObject;
import com.mofang.chat.guild.global.RedisKey;
import com.mofang.chat.guild.global.common.GuildUserAuditType;
import com.mofang.chat.guild.global.common.GuildUserRole;
import com.mofang.chat.guild.global.common.GuildUserStatus;
import com.mofang.chat.guild.model.Guild;
import com.mofang.chat.guild.model.GuildGroupUser;
import com.mofang.chat.guild.model.GuildUser;
import com.mofang.chat.guild.mysql.GuildGroupUserDao;
import com.mofang.chat.guild.mysql.GuildUserDao;
import com.mofang.chat.guild.mysql.impl.GuildGroupUserDaoImpl;
import com.mofang.chat.guild.mysql.impl.GuildUserDaoImpl;
import com.mofang.chat.guild.redis.GuildGameRedis;
import com.mofang.chat.guild.redis.GuildGroupRedis;
import com.mofang.chat.guild.redis.GuildGroupUserRedis;
import com.mofang.chat.guild.redis.GuildUserRedis;
import com.mofang.chat.guild.redis.ResultCacheRedis;
import com.mofang.chat.guild.redis.impl.GuildGameRedisImpl;
import com.mofang.chat.guild.redis.impl.GuildGroupRedisImpl;
import com.mofang.chat.guild.redis.impl.GuildGroupUserRedisImpl;
import com.mofang.chat.guild.redis.impl.GuildUserRedisImpl;
import com.mofang.chat.guild.redis.impl.ResultCacheRedisImpl;
import com.mofang.chat.guild.service.GuildUserService;
import com.mofang.framework.util.StringUtil;

/**
 * 
 * @author zhaodx
 *
 */
public class GuildUserServiceImpl implements GuildUserService
{
	private final static GuildUserServiceImpl SERVICE = new GuildUserServiceImpl();
	private GuildUserRedis guildUserRedis = GuildUserRedisImpl.getInstance();
	private GuildUserDao guildUserDao = GuildUserDaoImpl.getInstance();
	private GuildGroupRedis guildGroupRedis = GuildGroupRedisImpl.getInstance();
	private GuildGroupUserRedis guildGroupUserRedis = GuildGroupUserRedisImpl.getInstance();
	private GuildGroupUserDao guildGroupUserDao = GuildGroupUserDaoImpl.getInstance();
	private GuildGameRedis guildGameRedis = GuildGameRedisImpl.getInstance();
	private ResultCacheRedis resultCacheRedis = ResultCacheRedisImpl.getInstance();
	
	private GuildUserServiceImpl()
	{}
	
	public static GuildUserServiceImpl getInstance()
	{
		return SERVICE;
	}

	@Override
	public void join(final GuildUser model) throws Exception
	{
		try
		{
			///保存公会成员信息
			guildUserRedis.add(model);
			guildUserDao.add(model);
			
			///发送申请加入公会通知
			NotifyPushComponent.applyGuild(model.getUserId(), model.getGuildId());
		}
		catch(Exception e)
		{
			GlobalObject.ERROR_LOG.error("at GuildUserServiceImpl.join throw an error.", e);
			throw e;
		}
	}

	@Override
	public void changeRole(long guildId, long userId, int role) throws Exception
	{
		try
		{
			///更新用户角色
			guildUserRedis.updateRole(guildId, userId, role);
			guildUserDao.updateRole(guildId, userId, role);
			
			///发送角色变更通知
			if(role == GuildUserRole.ADMIN)   ///升为管理员
				NotifyPushComponent.changeAdmin(userId, guildId);
			else if(role == GuildUserRole.MEMBER)  ///降为普通会员
				NotifyPushComponent.changeMember(userId, guildId);
		}
		catch(Exception e)
		{
			GlobalObject.ERROR_LOG.error("at GuildUserServiceImpl.changeRole throw an error.", e);
			throw e;
		}
	}

	@Override
	public void delete(final long guildId, final long userId, int event) throws Exception
	{
		try
		{
			///将用户从公会成员中删除
			guildUserRedis.delete(guildId, userId);
			guildUserDao.delete(guildId, userId);
			
			///将用户从公会默认群组成员列表中删除
			Set<String> groupIds = guildGroupRedis.getGuildGroupList(guildId);
			if(null != groupIds)
			{
				long groupId = 0L;
				for(String groupIdStr : groupIds)
				{
					groupId = Long.parseLong(groupIdStr);
					guildGroupUserRedis.delete(groupId, userId);
					guildGroupUserDao.delete(groupId, userId);
				}
			}
			
			///将用户从公会游戏群组成员列表中删除
			Set<String> gameIds = guildGameRedis.getGameListByGuild(guildId);
			if(null != gameIds)
			{
				for(String gameId : gameIds)
				{
					Set<String> gameGroupIds = guildGroupRedis.getGuildGameGroupList(guildId, Integer.parseInt(gameId));
					if(null != gameGroupIds)
					{
						long groupId = 0L;
						for(String groupIdStr : gameGroupIds)
						{
							groupId = Long.parseLong(groupIdStr);
							if(guildGroupUserRedis.exists(groupId, userId))
							{
								guildGroupUserRedis.delete(groupId, userId);
								guildGroupUserDao.delete(groupId, userId);
							}
						}
					}
				}
			}
			
			///发送删除公会用户通知
			if(event == 1)  ///会员主动退出公会
				NotifyPushComponent.quitGuild(userId, guildId);
			else if(event == 2) ///管理员把用户从公会中踢出
				NotifyPushComponent.deleteMember(userId, guildId);
		}
		catch(Exception e)
		{
			GlobalObject.ERROR_LOG.error("at GuildUserServiceImpl.delete throw an error.", e);
			throw e;
		}
	}

	/**
	 * 异步审核
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
	public void audit(final Guild guild, final long applyUid, final int auditType) throws Exception
	{
		try
		{
			long guildId = guild.getGuildId();
			long createTime = guild.getCreateTime().getTime();
			if(auditType == GuildUserAuditType.AUDIT_PASS)
			{
				///更新成员状态
				guildUserRedis.updateStatus(guildId, createTime, applyUid, GuildUserStatus.NORMAL);
				guildUserDao.updateStatus(guildId, applyUid, GuildUserStatus.NORMAL);
				
				///将用户添加到公会默认群组
				Set<String> groupIds = guildGroupRedis.getGuildGroupList(guildId);
				if(null != groupIds)
				{
					long groupId = 0L;
					GuildGroupUser guildGroupUser = null;
					for(String groupIdStr : groupIds)
					{
						groupId = Long.parseLong(groupIdStr);
						///构建群组用户信息
						guildGroupUser = new GuildGroupUser();
						guildGroupUser.setGroupId(groupId);
						guildGroupUser.setUserId(applyUid);
						guildGroupUser.setReceiveNotify(1);
						guildGroupUser.setCreateTime(new Date());
						guildGroupUserRedis.add(guildGroupUser);
						guildGroupUserDao.add(guildGroupUser);
					}
				}
				
				///变更公会今日新增用户数
				guildUserRedis.incrNewMemberCount(guildId);
				
				///发送加入公会通知
				NotifyPushComponent.joinGuild(applyUid, guildId);
			}
			else if(auditType == GuildUserAuditType.AUDIT_NOT_PASS)
			{
				///更新成员的状体(审核未通过)
				guildUserRedis.updateStatus(guildId, createTime, applyUid, GuildUserStatus.AUDIT_NOT_PASS);
				guildUserDao.updateStatus(guildId, applyUid, GuildUserStatus.AUDIT_NOT_PASS);
			}
		}
		catch(Exception e)
		{
			GlobalObject.ERROR_LOG.error("at GuildUserServiceImpl.audit throw an error.", e);
			throw e;
		}
	}

	@Override
	public JSONArray getAllUserList(final long guildId) throws Exception
	{
		///如果结果缓存里有，直接返回
		String cacheKey = RedisKey.CACHE_GUILD_USER_LIST_KEY_PREFIX + guildId;
		String result = resultCacheRedis.getCache(cacheKey);
		if(!StringUtil.isNullOrEmpty(result))
		{
			JSONArray data = new JSONArray(result);
			return data;
		}
		
		///结果缓存没有，则需要重新构建列表信息，并将结果存入缓存中
		JSONArray data = new JSONArray();
		JSONObject userJson = null;
		///添加公会待审核成员
		Set<String> unauditedUserIds = guildUserRedis.getUnauditedUserList(guildId);
		if(null != unauditedUserIds)
		{
			for(String userId : unauditedUserIds)
			{
				userJson = getUserInfo(guildId, Long.parseLong(userId));
				if(null != userJson)
					data.put(userJson);
			}
		}
		///添加公会成员
		Set<String> userIds = guildUserRedis.getUserList(guildId);
		if(null != userIds)
		{
			for(String userId : userIds)
			{
				userJson = getUserInfo(guildId, Long.parseLong(userId));
				if(null != userJson)
					data.put(userJson);
			}
		}
		///将结果存入缓存中
		resultCacheRedis.saveCache(cacheKey, data.toString(), GlobalConfig.GUILD_USER_LIST_EXPIRE);
		return data;
	}
	
	private JSONObject getUserInfo(long guildId, long userId)
	{
		try
		{
			JSONObject json = new JSONObject();
			json.put("uid", userId);
			
			///获取用户基本信息
			User user = UserComponent.getInfo(userId);
			if(null != user)
			{
				json.put("nickname", user.getNickName());
				json.put("avatar", user.getAvatar());
				json.put("sex", user.getGender());
			}
			GuildUser guildUser = guildUserRedis.getInfo(guildId, userId);
			if(null != guildUser)
			{
				json.put("status", guildUser.getStatus());
				json.put("role", guildUser.getRole());
				json.put("last_login_time", guildUser.getLastLoginTime().getTime());
				json.put("postscript", guildUser.getPostscript());
			}
			return json;
		}
		catch(Exception e)
		{
			GlobalObject.ERROR_LOG.error("at GuildUserServiceImpl.getUserInfo throw an error. ", e);
			return null;
		}
	}
}