package com.mofang.chat.guild.logic;

import com.mofang.chat.guild.global.ResultValue;
import com.mofang.framework.web.server.reactor.context.HttpRequestContext;

/**
 * 
 * @author zhaodx
 *
 */
public interface GuildGameLogic
{
	public ResultValue edit(HttpRequestContext context) throws Exception;
	
	public ResultValue getGuildList(HttpRequestContext context) throws Exception;
	
	public ResultValue getGameList(HttpRequestContext context) throws Exception;
	
	public ResultValue getHotGuilds(HttpRequestContext context) throws Exception;
}