package com.mofang.chat.guild.redis;

import java.util.Date;
import java.util.Set;

import com.mofang.chat.guild.model.GuildUser;

/**
 * 
 * @author lenovo
 *
 */
public interface GuildUserRedis
{
	public boolean exists(long guildId, long userId) throws Exception;
	
	public boolean existsUnAudit(long guildId, long userId) throws Exception;
	
	public boolean add(GuildUser model) throws Exception;
	
	public boolean delete(long guildId, long userId) throws Exception;
	
	public boolean deleteByGuild(long guildId) throws Exception;
	
	public boolean updateRole(long guildId, long userId, int role) throws Exception;
	
	public boolean updateStatus(long guildId, long guildScore, long userId, int status) throws Exception;
	
	public boolean updateLastLoginTime(long guildId, long userId, Date lastLoginTime) throws Exception;
	
	public GuildUser getInfo(long guildId, long userId) throws Exception;
	
	public Set<String> getUserList(long guildId) throws Exception;
	
	public long getUserCount(long guildId) throws Exception;
	
	public Set<String> getUnauditedUserList(long guildId) throws Exception;
	
	public boolean incrNewMemberCount(long guildId) throws Exception;
	
	public boolean clearNewMemberCount() throws Exception;
	
	public int getNewMemberCount(long guildId) throws Exception;
	
	public boolean setUnloginMemberCount7Days(long guildId, int count) throws Exception;
	
	public boolean clearUnloginMemberCount7Days() throws Exception;
	
	public int getUnloginMemberCount7Days(long guildId) throws Exception;
	
	public boolean setUnloginMemberCount30Days(long guildId, int count) throws Exception;
	
	public boolean clearUnloginMemberCount30Days() throws Exception;
	
	public int getUnloginMemberCount30Days(long guildId) throws Exception;
}