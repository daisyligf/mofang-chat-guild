package com.mofang.chat.guild.cron.task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import com.mofang.chat.guild.global.GlobalObject;
import com.mofang.chat.guild.mysql.GuildUserDao;
import com.mofang.chat.guild.mysql.impl.GuildUserDaoImpl;
import com.mofang.chat.guild.redis.GuildUserRedis;
import com.mofang.chat.guild.redis.impl.GuildUserRedisImpl;

/**
 * 公会7天未登录用户数更新任务
 * @author zhaodx
 *
 */
public class GuildUnloginMemberCount7DaysUpdateTask implements Runnable
{
	private final static long TIME_AGO = 7 * 24 * 60 * 60 * 1000L;
	private GuildUserRedis guildUserRedis = GuildUserRedisImpl.getInstance();
	private GuildUserDao guildUserDao = GuildUserDaoImpl.getInstance();

	@Override
	public void run()
	{
		try
		{
			///清空公会7天未登录用户数
			guildUserRedis.clearUnloginMemberCount7Days();
			
			///填充公会7天未登录用户数
			long timeOld = System.currentTimeMillis() - TIME_AGO;
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
			String dateAgo = format.format(new Date(timeOld));
			Map<Long, Integer> map = guildUserDao.getUnloginMemberCount(dateAgo);
			if(null == map)
				return;
			
			Iterator<Long> iterator = map.keySet().iterator();
			long guildId = 0L;
			int count = 0;
			while(iterator.hasNext())
			{
				guildId = iterator.next();
				count = map.get(guildId);
				guildUserRedis.setUnloginMemberCount7Days(guildId, count);
			}
			GlobalObject.INFO_LOG.info("GuildUnloginMemberCount7DaysUpdateTask execute completed");
		}
		catch(Exception e)
		{
			GlobalObject.ERROR_LOG.error("at GuildUnloginMemberCount7DaysUpdateTask.run throw an error. ", e);
		}
	}
}