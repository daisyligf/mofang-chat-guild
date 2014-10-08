package com.mofang.chat.guild.cron;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.mofang.chat.guild.cron.task.GuildDismissTask;
import com.mofang.chat.guild.cron.task.GuildNewMemberCountClearTask;
import com.mofang.chat.guild.cron.task.GuildUnloginMemberCount30DaysUpdateTask;
import com.mofang.chat.guild.cron.task.GuildUnloginMemberCount7DaysUpdateTask;
import com.mofang.chat.guild.cron.task.HotGuildRankUpdateTask;
import com.mofang.chat.guild.cron.task.NewGuildListUpdateTask;
import com.mofang.chat.guild.global.GlobalConfig;
import com.mofang.chat.guild.global.GlobalObject;

/**
 * 定时任务启动类
 * @author zhaodx
 *
 */
public class CrontabBootstrap implements Runnable
{
	@Override
	public void run()
	{
	    	Document doc = null;
		try
		{
		    	CrontabManager cron = new CrontabManager();
			SAXReader reader = new SAXReader();
			doc = reader.read(new File(GlobalConfig.CRON_TASK_CONFIG_PATH));
			Element root = doc.getRootElement();
			
			@SuppressWarnings("unchecked")
			List<Element> list = root.elements("task");
			for (Element obj: list)
			{
			    String className = obj.elementText("class");
			    Runnable task = (Runnable) Class.forName(className).newInstance();
			    String startTime = obj.elementText("startTime");
			    boolean startup = Boolean.parseBoolean(obj.elementText("startup"));
			    TaskEntity taskEntity = buildTask(startTime, task);
			    if (startup)
			    {
				cron.add(taskEntity);
				GlobalObject.INFO_LOG.info("Task Class:" + className + ",StartTime:" + startTime + " added");
			    }
			}
			cron.execute();
			GlobalObject.INFO_LOG.info("CrontabBootstrap.run finished.");
		}
		catch (Exception e)
		{
		    GlobalObject.ERROR_LOG.error("CrontabBootstrap.run throws an error.", e);
		}
	}
	
	/**
	 * 构建解散公会的定时任务
	 * @return
	 */
	private TaskEntity buildGuildDismissTask()
	{
		String startTime = GlobalConfig.GUILD_DISMISS_TASK_TIME;
		Runnable task = new GuildDismissTask();
		return buildTask(startTime, task);
	}
	
	/**
	 * 构建火热公会排名更新的定时任务
	 * @return
	 */
	private TaskEntity buildHotGuildRankUpdateTask()
	{
		String startTime = GlobalConfig.HOT_GUILD_RANK_UPDATE_TASK_TIME;
		Runnable task = new HotGuildRankUpdateTask();
		return buildTask(startTime, task);
	}
	
	/**
	 * 构建新锐公会列表更新的定时任务
	 * @return
	 */
	private TaskEntity buildNewGuildListUpdateTask()
	{
	    	String startTime = GlobalConfig.NEW_GUILD_LIST_UPDATE_TASK_TIME;
	    	Runnable task = new NewGuildListUpdateTask();
	    	return buildTask(startTime, task);
	}
	
	/**
	 * 构建清空公会今日新增用户数的定时任务
	 * @return
	 */
	private TaskEntity buildGuildNewMemberCountClearTask()
	{
		String startTime = GlobalConfig.GUILD_NEW_MEMBER_COUNT_CLEAR_TASK_TIME;
		Runnable task = new GuildNewMemberCountClearTask();
		return buildTask(startTime, task);
	}
	
	/**
	 * 构建更新公会7天未登录用户数的定时任务
	 * @return
	 */
	private TaskEntity buildGuildUnloginMemberCount7DaysUpdateTask()
	{
		String startTime = GlobalConfig.GUILD_UNLOGIN_MEMBER_COUNT_7DAYS_CLEAR_TASK_TIME;
		Runnable task = new GuildUnloginMemberCount7DaysUpdateTask();
		return buildTask(startTime, task);
	}
	
	/**
	 * 构建更新公会30天未登录用户数的定时任务
	 * @return
	 */
	private TaskEntity buildGuildUnloginMemberCount30DaysUpdateTask()
	{
		String startTime = GlobalConfig.GUILD_UNLOGIN_MEMBER_COUNT_30DAYS_CLEAR_TASK_TIME;
		Runnable task = new GuildUnloginMemberCount30DaysUpdateTask();
		return buildTask(startTime, task);
	}
	
	private TaskEntity buildTask(String startTime, Runnable task)
	{
		TaskEntity entity = new TaskEntity();
		long oneDay = 24 * 60 * 60 * 1000;  
	    long initDelay  = getTimeMillis(startTime) - System.currentTimeMillis();  
	    initDelay = initDelay > 0 ? initDelay : oneDay + initDelay;  
		entity.setInitialDelay(initDelay);
		entity.setPeriod(oneDay);
		entity.setUnit(TimeUnit.MILLISECONDS);
		entity.setTask(task);
		return entity;
	}
	
	/**
	 * 获取指定时间对应的毫秒数
	 * @param time "HH:mm:ss"
	 * @return
	 */
	private long getTimeMillis(String time) 
	{
		try
		{
			DateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
			DateFormat dayFormat = new SimpleDateFormat("yy-MM-dd");
			Date curDate = dateFormat.parse(dayFormat.format(new Date()) + " " + time);
			return curDate.getTime();
		} 
		catch (ParseException e) 
		{
			e.printStackTrace();
		}
		return 0;
	}
}