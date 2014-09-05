package com.mofang.chat.guild.mysql;

import java.util.List;

import com.mofang.chat.guild.model.GuildRecruit;

/**
 * 
 * @author zhaodx
 *
 */
public interface GuildRecruitDao
{
	public void add(GuildRecruit model) throws Exception;
	
	public void updateStatus(int recruitId, int status) throws Exception;
	
	public void updateStatusByGuild(long guildId, int status) throws Exception;
	
	public void delete(int recruitId) throws Exception;
	
	public void deleteByGuild(long guildId) throws Exception;
	
	public GuildRecruit getInfo(int recruitId) throws Exception;
	
	public List<GuildRecruit> getList(long guildId, int status) throws Exception;
}