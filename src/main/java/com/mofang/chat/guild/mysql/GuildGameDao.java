package com.mofang.chat.guild.mysql;

import com.mofang.chat.guild.model.GuildGame;

/**
 * 
 * @author zhaodx
 *
 */
public interface GuildGameDao
{
	public void add(GuildGame model) throws Exception;
	
	public void delete(long guildId, int gameId) throws Exception;
	
	public void deleteByGuild(long guildId) throws Exception;
}