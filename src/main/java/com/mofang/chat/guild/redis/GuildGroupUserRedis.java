package com.mofang.chat.guild.redis;

import java.util.Map;

import com.mofang.chat.guild.model.GuildGroupUser;

/**
 * 
 * @author zhaodx
 *
 */
public interface GuildGroupUserRedis
{
	public boolean add(GuildGroupUser model) throws Exception;
	
	public boolean exists(long groupId, long userId) throws Exception;
	
	public boolean updateReceiveNotify(long groupId, long userId, int receiveNotify) throws Exception;
	
	public String getRecevieNotify(long groupId, long userId) throws Exception;
	
	public boolean delete(long groupId, long userId) throws Exception;
	
	public boolean deleteByGroup(long groupId) throws Exception;
	
	public Map<String, String> getUserList(long groupId) throws Exception;
	
	public long getUserCount(long groupId) throws Exception;
}