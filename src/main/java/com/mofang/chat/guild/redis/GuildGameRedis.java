package com.mofang.chat.guild.redis;

import java.util.Set;

import com.mofang.chat.guild.model.GuildGame;

/**
 * 
 * @author zhaodx
 *
 */
public interface GuildGameRedis
{
	public boolean add(GuildGame model) throws Exception;
	
	public boolean delete(long guildId, int gameId) throws Exception;
	
	public Set<String> getGameListByGuild(long guildId) throws Exception;
	
	public long getGameCountByGuild(long guildId) throws Exception;
	
	public Set<String> getGuildListByGame(int gameId, int start, int end) throws Exception;
	
	public long getGuildCountByGame(int gameId) throws Exception;
}