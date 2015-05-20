package com.mofang.chat.guild.cron.task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import com.mofang.chat.guild.component.CheckInComponent;
import com.mofang.chat.guild.global.GlobalConfig;
import com.mofang.chat.guild.global.GlobalObject;
import com.mofang.chat.guild.model.Guild;
import com.mofang.chat.guild.mysql.GuildDao;
import com.mofang.chat.guild.mysql.impl.GuildDaoImpl;
import com.mofang.chat.guild.redis.GuildRedis;
import com.mofang.chat.guild.redis.GuildUserRedis;
import com.mofang.chat.guild.redis.impl.GuildRedisImpl;
import com.mofang.chat.guild.redis.impl.GuildUserRedisImpl;

/**
 * 火热公会排序更新任务
 * 最热公会跟进公会的人数+签到数两项数据相加获得的总数倒序排列得到
 * 需要强调的是，人数和权重数相加时，占比分别为（人数）70%，（签到数）30%
 * @author zhaodx
 *
 */
public class HotGuildRankUpdateTask implements Runnable
{
	private GuildRedis guildRedis = GuildRedisImpl.getInstance();
	private GuildDao guildDao = GuildDaoImpl.getInstance();
	private GuildUserRedis guildUserRedis = GuildUserRedisImpl.getInstance();

	@Override
	public void run()
	{
		try
		{
			Set<String> guildIds = guildRedis.getHotList(0, Integer.MAX_VALUE);
			if(null == guildIds || guildIds.size() == 0)
				return;
			
			SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
			long lastDayTime = System.currentTimeMillis() - (24 * 60 * 60 * 1000);
			String lastDate = format.format(new Date(lastDayTime));
			
			long guildId = 0L;
			Guild guildInfo = null;
			for(String strGuildId : guildIds)
			{
				guildId = Long.parseLong(strGuildId);
				guildInfo = guildRedis.getInfo(guildId);
				if(null == guildInfo)
					continue;
				
				///获取公会会员数
				long memberCount = guildUserRedis.getUserCount(guildId);
				Map<String, Integer> checkinMap = CheckInComponent.getGuildCheckinNum(guildId, lastDate);
				int markCount = checkinMap.get("num");
				
				///计算公会热度
				double memberScore = memberCount * GlobalConfig.HOT_GUILD_RANK_MEMBER_RATE;
				double markScore = markCount * GlobalConfig.HOT_GUILD_RANK_MARK_RATE;
				double hot = memberScore + markScore;
				
				///更新公会热度值
				guildInfo.setHot(hot);
				guildRedis.update(guildInfo);
				guildDao.update(guildInfo);
				
				///更新排名
				double rankScore = guildInfo.getHotSeq() + hot;
				guildRedis.updateHotList(guildId, rankScore);
			}
			GlobalObject.INFO_LOG.info("HotGuildRankUpdateTask execute completed");
		}
		catch(Exception e)
		{
			GlobalObject.ERROR_LOG.error("at HotGuildRankUpdateTask.run throw an error. ", e);
		}
	}
}