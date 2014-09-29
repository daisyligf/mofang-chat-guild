package com.mofang.chat.guild.cron.task;

import java.util.List;

import com.mofang.chat.guild.global.GlobalObject;
import com.mofang.chat.guild.model.Guild;
import com.mofang.chat.guild.mysql.GuildDao;
import com.mofang.chat.guild.mysql.impl.GuildDaoImpl;
import com.mofang.chat.guild.redis.GuildRedis;
import com.mofang.chat.guild.redis.impl.GuildRedisImpl;

/**
 * 
 * @author daisyli
 *
 */
public class GuildFieldsInitTask implements Runnable {
    

    private GuildDao guildDao = GuildDaoImpl.getInstance();
    private GuildRedis guildRedis = GuildRedisImpl.getInstance();
    
    public void run() 
    {
	try 
	{
	    List<Guild> list = guildDao.getAll();
	    for (Guild guild : list) {
		String guildName = guild.getGuildName();
		String guildNamePrefix = guild.getGuildNamePrefix();
		String intro = guild.getIntro();
		String notice = guild.getNotice();
		
		guild.setGuildNameOriginal(guildName);
		guild.setGuildNameMark(guildName);
		
		guild.setGuildNamePrefixOriginal(guildNamePrefix);
		guild.setGuildNamePrefixMark(guildNamePrefix);
		
		guild.setIntroOriginal(intro);
		guild.setIntroMark(intro);
		
		guild.setNoticeOriginal(notice);
		guild.setNoticeMark(notice);
		guildDao.update(guild);
		guildRedis.update(guild);
		
	    }
	    GlobalObject.INFO_LOG.info("GuildFieldsInitTask execute completed");
	}
	catch (Exception e) 
	{
	    GlobalObject.ERROR_LOG.error("at GuildFieldsInitTask.run throw an error. ", e);
	}
    }
}
