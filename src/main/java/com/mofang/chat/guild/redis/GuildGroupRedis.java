package com.mofang.chat.guild.redis;

import java.util.Set;

import com.mofang.chat.guild.model.GuildGroup;

/**
 * 
 * @author zhaodx
 *
 */
public interface GuildGroupRedis
{
	public boolean initMaxId() throws Exception;
	
	public long getMaxId() throws Exception;
	
	public boolean add(GuildGroup model) throws Exception;
	
	public boolean delete(long groupId) throws Exception;
	
	public boolean deleteByGuild(long guildId) throws Exception;
	
	public boolean deleteByGame(long guildId, int gameId) throws Exception;
	
	public GuildGroup getInfo(long groupId) throws Exception;
	
	public Set<String> getGuildGroupList(long guildId) throws Exception;
	
	public Set<String> getGuildGameGroupList(long guildId, int gameId) throws Exception;
}