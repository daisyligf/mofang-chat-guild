package com.mofang.chat.guild.mysql;

import java.util.List;

import com.mofang.chat.guild.model.Guild;

/**
 * 
 * @author zhaodx
 *
 */
public interface GuildDao
{
	public void add(Guild model) throws Exception;
	
	public void update(Guild model) throws Exception;
	
	public void delete(long guildId) throws Exception;
	
	public void updateStatus(long guildId, int status) throws Exception;
	
	public Guild getInfo(long guildId) throws Exception;
	
	public long getCreatedCount(long userId) throws Exception;
	
	public List<Long> getDismissGuildIds(String dateAgo, int minMemberCount) throws Exception;
	
	public List<Long> getInactiveGuildIds(String date, int minMemberCount) throws Exception;
	
	public void updateDismissTime(long guildId, String date) throws Exception;
	
	public List<Guild> getNewGuildList(int minMemberCount) throws Exception;
	
	public List<Guild> getAll() throws Exception;
	
	public List<Long> getNormalGuildIds() throws Exception;
}