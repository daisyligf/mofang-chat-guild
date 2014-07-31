package com.mofang.chat.guild.redis;

public interface GuildGroupMessageRedis
{
	public int getUnreadCount(long userId, long groupId) throws Exception;
}