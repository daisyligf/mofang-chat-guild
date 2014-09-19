package com.mofang.chat.guild.cron.task;

import java.util.List;

import com.mofang.chat.guild.global.GlobalConfig;
import com.mofang.chat.guild.global.GlobalObject;
import com.mofang.chat.guild.model.Guild;
import com.mofang.chat.guild.mysql.GuildDao;
import com.mofang.chat.guild.mysql.impl.GuildDaoImpl;
import com.mofang.chat.guild.redis.GuildRedis;
import com.mofang.chat.guild.redis.impl.GuildRedisImpl;

/**
 * 
 * 新锐公会列表更新定时任务
 * @author daisyli
 *
 */
public class NewGuildListUpdateTask implements Runnable 
{

    private GuildRedis guildRedis = GuildRedisImpl.getInstance();
    private GuildDao guildDao = GuildDaoImpl.getInstance();
    
    public void run() 
    {
	try 
	{
	    List<Guild> guildList = guildDao.getNewGuildList(GlobalConfig.MIN_NEW_GUILD_LIST_MEMBER);
	    // 1.清空newGuildList列表
	    guildRedis.clearNewList();
	    // 2.依次添加到newGuildList
	    for (Guild guild : guildList) {
		guildRedis.addToNewList(guild);
	    }
	    
	    GlobalObject.INFO_LOG.info("NewGuildListUpdateTask execute completed");
	}
	catch (Exception e) 
	{
	    GlobalObject.ERROR_LOG.error("at NewGuildListUpdateTask.run throw an error. ", e);
	}
    }
}
