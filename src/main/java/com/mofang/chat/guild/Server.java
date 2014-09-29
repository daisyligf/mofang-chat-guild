package com.mofang.chat.guild;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.mofang.chat.guild.cron.CrontabBootstrap;
import com.mofang.chat.guild.cron.task.GuildUserListUpdateExecutor;
import com.mofang.chat.guild.global.GlobalConfig;
import com.mofang.chat.guild.init.Initializer;
import com.mofang.chat.guild.init.impl.MainInitializer;
import com.mofang.framework.web.server.action.ActionResolve;
import com.mofang.framework.web.server.action.impl.DefaultHttpActionResolve;
import com.mofang.framework.web.server.conf.ChannelConfig;
import com.mofang.framework.web.server.main.WebServer;
import com.mofang.framework.web.server.reactor.parse.PostDataParserType;

public class Server 
{
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		//String configpath = "E:\\WorkSpace\\mofang.chat.guild\\src\\main\\resources\\config.ini";
		
		if(args.length <= 0)
		{
			System.out.println("usage:java -server -Xms1024m -Xmx1024m -jar mofang-chat-guild.jar configpath");
			System.exit(1);
		}
		String configpath = args[0];
		
		try
		{
			///服务器初始化
			System.out.println("prepare to initializing config......");
			Initializer initializer = new MainInitializer(configpath);
			initializer.init();
			System.out.println("initialize config completed!");
			
			///启动定时任务
			Thread timeJob = new Thread(new CrontabBootstrap());
			timeJob.start();
			
			// 启动后台任务
			int THREADS = Runtime.getRuntime().availableProcessors() + 1;
			ScheduledExecutorService executor = Executors.newScheduledThreadPool(THREADS);
			executor.scheduleAtFixedRate(new GuildUserListUpdateExecutor(), 0, GlobalConfig.GUILD_USER_LIST_UPDATE_INTERVAL, TimeUnit.SECONDS);
			
			///启动服务器
			ActionResolve httpActionResolve = new DefaultHttpActionResolve();
			int port = GlobalConfig.SERVER_PORT;
			WebServer server = new WebServer(port, PostDataParserType.Json);
			
			///channel 配置
			ChannelConfig channelConfig = new ChannelConfig();
			channelConfig.setConnTimeout(GlobalConfig.CONN_TIMEOUT);
			channelConfig.setSoTimeout(GlobalConfig.READ_TIMEOUT);
			
			server.setChannelConfig(channelConfig);
			server.setScanPackagePath(GlobalConfig.SCAN_PACKAGE_PATH);
			server.setHttpActionResolve(httpActionResolve);
			try
			{
				System.out.println("Server Start on " + GlobalConfig.SERVER_PORT);
				server.start();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		catch (Exception e)
		{
			System.out.println("server start error. message:");
			e.printStackTrace();
		}
	}
}