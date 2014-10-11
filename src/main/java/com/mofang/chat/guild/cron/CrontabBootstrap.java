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
			    
			    String type = obj.elementText("type");
			    if (type == null) break;
			    long period = 24 * 60 * 60 * 1000;
			    if (type.equals("interval")) 
			    {
				period = Long.valueOf(obj.elementText("period")) * 1000;
			    }
			    
			    TaskEntity taskEntity = buildTask(startTime, period, task);
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
	
	private TaskEntity buildTask(String startTime, long period, Runnable task)
	{
		TaskEntity entity = new TaskEntity();
		long initDelay = 0L;
		if (!startTime.equals("0")) {
		    initDelay  = getTimeMillis(startTime) - System.currentTimeMillis();  
		    initDelay = initDelay > 0 ? initDelay : period + initDelay; 
		}
	     
		entity.setInitialDelay(initDelay);
		entity.setPeriod(period);
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