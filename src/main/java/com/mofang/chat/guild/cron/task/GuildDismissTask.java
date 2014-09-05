package com.mofang.chat.guild.cron.task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.mofang.chat.guild.component.NotifyPushComponent;
import com.mofang.chat.guild.global.GlobalConfig;
import com.mofang.chat.guild.global.GlobalObject;
import com.mofang.chat.guild.global.common.GuildStatus;
import com.mofang.chat.guild.model.Guild;
import com.mofang.chat.guild.mysql.GuildDao;
import com.mofang.chat.guild.mysql.impl.GuildDaoImpl;
import com.mofang.chat.guild.redis.GuildRedis;
import com.mofang.chat.guild.redis.impl.GuildRedisImpl;
import com.mofang.chat.guild.service.GuildService;
import com.mofang.chat.guild.service.impl.GuildServiceImpl;

/**
 * 活跃度未达标公会解散任务(公会创建15个自然日后，会员数少于10人的公会需要解散)
 * @author zhaodx
 *
 */
public class GuildDismissTask implements Runnable
{
	private final static long TIME_AGO = GlobalConfig.GUILD_CHECK_DAYS * 24 * 60 * 60 * 1000;
	private final static long FIRST_ALARM_DAY = GlobalConfig.GUILD_FIRST_ALARM_DAYS * 24 * 60 * 60 * 1000;
	private final static long SECOND_ALARM_DAY = GlobalConfig.GUILD_SECOND_ALARM_DAYS * 24 * 60 * 60 * 1000;
	private GuildRedis guildRedis = GuildRedisImpl.getInstance();
	private GuildDao guildDao = GuildDaoImpl.getInstance();
	private GuildService guildService = GuildServiceImpl.getInstance();
	
	@Override
	public void run()
	{
		try
		{
		    	// 对于创建15天及以上但人数仍然少于10人的公会进行系统删除
			long timeOld = System.currentTimeMillis() - TIME_AGO;
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
			String dateAgo = format.format(new Date(timeOld));
			int minMemberCount = GlobalConfig.GUILD_MIN_MEMBER_COUNT;
			List<Long> guildIds = guildDao.getDismissGuildIds(dateAgo, minMemberCount);
			if(guildIds != null && guildIds.size() > 0) {
        			Guild guildInfo = null;
        			for(Long guildId : guildIds)
        			{
        				guildInfo = guildRedis.getInfo(guildId);
        				if(null == guildInfo)
        					continue;
        				guildService.delete(guildInfo, GuildStatus.DISMISS, 2);
        				guildService.updateDismissTime(guildId);
        			}
			}
			
			// 对于创建第10天的公会进行提醒
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			long firstAlarmTime = System.currentTimeMillis() - FIRST_ALARM_DAY;
			String firstAlarmDate = dateFormat.format(new Date(firstAlarmTime));
			List<Long> firstAlarmGuildIds = guildDao.getInactiveGuildIds(firstAlarmDate, minMemberCount);
			if (firstAlarmGuildIds != null && firstAlarmGuildIds.size() > 0) {
        			for (long guildId : firstAlarmGuildIds) {
        			    Guild guild = guildDao.getInfo(guildId);
        			    if (guild == null) continue;
        			    NotifyPushComponent.beforeDismiss(guild, 1);
        			}
			}
			
			// 对于创建第13天的公会进行提醒
			long secondAlarmTime = System.currentTimeMillis() - SECOND_ALARM_DAY;
			String secondAlarmDate = dateFormat.format(new Date(secondAlarmTime));
			List<Long> secondAlarmGuildIds = guildDao.getInactiveGuildIds(secondAlarmDate, minMemberCount);
			if (secondAlarmGuildIds != null && secondAlarmGuildIds.size() > 0) {
			    for (long guildId : secondAlarmGuildIds) {
				    Guild guild = guildDao.getInfo(guildId);
				    if (guild == null) continue;
				    NotifyPushComponent.beforeDismiss(guild, 2);
				}
			}
			
			
			GlobalObject.INFO_LOG.info("GuildDismissTask execute completed");
		}
		catch(Exception e)
		{
			GlobalObject.ERROR_LOG.error("at GuildDismissTask.run throw an error. ", e);
		}
	}
}