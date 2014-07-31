package com.mofang.chat.guild.cron.task;

import com.mofang.chat.guild.global.GlobalObject;
import com.mofang.chat.guild.redis.GuildUserRedis;
import com.mofang.chat.guild.redis.impl.GuildUserRedisImpl;

/**
 * 公会今日新增用户清空任务
 * @author zhaodx
 *
 */
public class GuildNewMemberCountClearTask implements Runnable
{
	private GuildUserRedis guildUserRedis = GuildUserRedisImpl.getInstance();

	@Override
	public void run()
	{
		try
		{
			guildUserRedis.clearNewMemberCount();
			GlobalObject.INFO_LOG.info("GuildNewMemberCountClearTask execute completed");
		}
		catch(Exception e)
		{
			GlobalObject.ERROR_LOG.error("at GuildNewMemberCountClearTask.run throw an error. ", e);
		}
	}
}