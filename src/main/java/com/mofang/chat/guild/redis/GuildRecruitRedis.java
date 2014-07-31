package com.mofang.chat.guild.redis;

import java.util.Set;

/**
 * 
 * @author zhaodx
 *
 */
public interface GuildRecruitRedis
{
	public boolean addAuditList(long guildId, long createTime) throws Exception;
	
	public boolean deleteByGuild(long guildId) throws Exception;
	
	public Set<String> getAuditList() throws Exception;
}