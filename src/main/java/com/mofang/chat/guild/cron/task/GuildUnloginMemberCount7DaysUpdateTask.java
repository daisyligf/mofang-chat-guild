package com.mofang.chat.guild.cron.task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mofang.chat.guild.global.GlobalObject;
import com.mofang.chat.guild.mysql.GuildUserDao;
import com.mofang.chat.guild.mysql.impl.GuildUserDaoImpl;
import com.mofang.chat.guild.redis.GuildUserRedis;
import com.mofang.chat.guild.redis.impl.GuildUserRedisImpl;
import com.mofang.chat.guild.service.GuildUserService;
import com.mofang.chat.guild.service.impl.GuildUserServiceImpl;

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
	private GuildUserService guildUserService = GuildUserServiceImpl.getInstance();

	@Override
	public void run()
	{	
	    	GlobalObject.INFO_LOG.info("entering GuildUnloginMemberCount7DaysUpdateTask.");
	    	long start = System.currentTimeMillis();
		try
		{
			///清空公会7天未登录用户数
			guildUserRedis.clearUnloginMemberCount7Days();
			guildUserRedis.clearUnloginMemberList7Days();
			
			///填充公会7天未登录用户数
			long timeOld = System.currentTimeMillis() - TIME_AGO;
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
			String dateAgo = format.format(new Date(timeOld));
			
			Map<Long, List<Long>> guildUsersMap = guildUserDao.getUnloginMemberMap(dateAgo);
			if (null == guildUsersMap)
				return;

			Iterator<Long> iterator = guildUsersMap.keySet().iterator();
			long guildId = 0L;
			int count = 0;
			List<Long> userIds = null;
			while (iterator.hasNext())
			{
			    guildId = iterator.next();
			    userIds = guildUsersMap.get(guildId);
			    if (userIds != null && userIds.size() > 0) {
				count = userIds.size();
				guildUserRedis.setUnloginMemberCount7Days(guildId, count);
				JSONArray memberList = new JSONArray();
				for (Long userId : userIds) 
				{
				    
				    JSONObject user = guildUserService.getUserInfo(guildId, userId);
				    memberList.put(user);
				}
				guildUserRedis.setUnloginMemberList7Days(guildId, memberList.toString());
			    }
			}
			long end = System.currentTimeMillis();
			
			GlobalObject.INFO_LOG.info("GuildUnloginMemberCount7DaysUpdateTask costs time " + (end - start)/1000 + " s" );
			GlobalObject.INFO_LOG.info("GuildUnloginMemberCount7DaysUpdateTask execute completed");
		}
		catch(Exception e)
		{
			GlobalObject.ERROR_LOG.error("at GuildUnloginMemberCount7DaysUpdateTask.run throw an error. ", e);
		}
	}
}