package com.mofang.chat.guild.mysql;

import java.util.List;

import com.mofang.chat.guild.model.GuildGroupUser;

/**
 * 
 * @author zhaodx
 *
 */
public interface GuildGroupUserDao
{
	public void add(GuildGroupUser model) throws Exception;
	
	public void updateReceiveNotify(long groupId, long userId, int receiveNotify) throws Exception;
	
	public void delete(long groupId, long userId) throws Exception;
	
	public void deleteByGroup(long groupId) throws Exception;
	
	public List<GuildGroupUser> getList(long groupId) throws Exception;
}