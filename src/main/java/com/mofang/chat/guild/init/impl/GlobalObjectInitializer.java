package com.mofang.chat.guild.init.impl;

import com.mofang.chat.guild.init.AbstractInitializer;
import com.mofang.chat.guild.global.GlobalConfig;
import com.mofang.chat.guild.global.GlobalObject;

/**
 * 
 * @author zhaodx
 *
 */
public class GlobalObjectInitializer extends AbstractInitializer
{
	@Override
	public void load() throws Exception
	{
		GlobalObject.initRedisMaster(GlobalConfig.REDIS_MASTER_CONFIG_PATH);
		GlobalObject.initRedisSlave(GlobalConfig.REDIS_SLAVE_CONFIG_PATH);
		GlobalObject.initChatSlave(GlobalConfig.CHAT_SLAVE_CONFIG_PATH);
		GlobalObject.initMysql(GlobalConfig.MYSQL_CONFIG_PATH);
		GlobalObject.initApiHttpClient(GlobalConfig.HTTP_CLIENT_API_CONFIG_PATH);
		GlobalObject.initChatServiceHttpClient(GlobalConfig.HTTP_CLIENT_CHATSERVICE_CONFIG_PATH);
	}
}