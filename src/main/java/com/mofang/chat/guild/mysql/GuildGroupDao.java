package com.mofang.chat.guild.mysql;

import com.mofang.chat.guild.model.GuildGroup;

/**
 * 
 * @author zhaodx
 *
 */
public interface GuildGroupDao
{
	public void add(GuildGroup model) throws Exception;
	
	public void update(GuildGroup model) throws Exception;
	
	public void delete(long groupId) throws Exception;
	
	public void deleteByGuildId(long guildId) throws Exception;
	
	public GuildGroup getInfo(long groupId) throws Exception;
}