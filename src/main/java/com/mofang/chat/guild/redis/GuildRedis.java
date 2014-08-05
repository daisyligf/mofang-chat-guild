package com.mofang.chat.guild.redis;

import java.util.Set;

import com.mofang.chat.guild.model.Guild;

/**
 * 
 * @author zhaodx
 *
 */
public interface GuildRedis
{
	public boolean initMaxId() throws Exception;
	
	public Long getMaxId() throws Exception;
	
	public boolean save(Guild model) throws Exception;
	
	public boolean update(Guild model) throws Exception;
	
	public boolean delete(long guildId) throws Exception;
	
	public boolean updateStatus(long guildId, int status) throws Exception;
	
	public boolean updateHotSequence(long guildId, long sequence, double hot) throws Exception;
	
	public boolean updateNewSequence(long guildId, long sequence, long createtime) throws Exception;

	public boolean updateHotList(long guildId, double score) throws Exception;
	
	public Guild getInfo(long guildId) throws Exception;
	
	public Set<String> getHotList(int start, int end) throws Exception;
	
	public long getHotCount() throws Exception;
	
	public Set<String> getNewList(int start, int end) throws Exception;
	
	public long getNewCount() throws Exception;
	
	public Set<String> getMyList(long userId) throws Exception;
	
	public long getMyCount(long userId) throws Exception;
	
	public long getRank(long guildId) throws Exception;
}