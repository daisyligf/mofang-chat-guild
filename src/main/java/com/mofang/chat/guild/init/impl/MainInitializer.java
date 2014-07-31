package com.mofang.chat.guild.init.impl;

import org.apache.log4j.PropertyConfigurator;

import com.mofang.chat.guild.init.AbstractInitializer;
import com.mofang.chat.guild.init.Initializer;
import com.mofang.chat.guild.global.GlobalConfig;

/**
 * 
 * @author zhaodx
 *
 */
public class MainInitializer extends AbstractInitializer
{
	private String configPath;
	
	public MainInitializer(String configPath)
	{
		this.configPath = configPath;
	}
	
	@Override
	public void load() throws Exception
	{
		Initializer globalConf = new GlobalConfigInitializer(configPath);
		globalConf.init();
		
		PropertyConfigurator.configure(GlobalConfig.LOG4J_CONFIG_PATH);
		
		Initializer globalObject = new GlobalObjectInitializer();
		globalObject.init();
		
		Initializer redisData = new RedisDataInitializer();
		redisData.init();
	}
}