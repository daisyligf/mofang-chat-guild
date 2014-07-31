package com.mofang.chat.guild.mysql;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.mofang.chat.guild.model.GuildUser;

/**
 * 
 * @author zhaodx
 *
 */
public interface GuildUserDao
{
	public void add(GuildUser model) throws Exception;
	
	public void delete(long guildId, long userId) throws Exception;
	
	public void deleteByGuild(long guildId) throws Exception;
	
	public void updateRole(long guildId, long userId, int role) throws Exception;
	
	public void updateStatus(long guildId, long userId, int status) throws Exception;
	
	/**
	 * 上次进入公会时间(拉取我的公会时触发)
	 * @param model
	 * @return
	 * @throws Exception
	 */
	public void updateLastLoginTime(long guildId, long userId, Date datetime) throws Exception;
	
	public List<GuildUser> getList(long guildId) throws Exception;
	
	public long getJoinCount(long userId) throws Exception;
	
	public Map<Long, Integer> getUnloginMemberCount(String dateAgo) throws Exception;
}