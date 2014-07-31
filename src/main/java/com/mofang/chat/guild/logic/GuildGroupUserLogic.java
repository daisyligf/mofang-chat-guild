package com.mofang.chat.guild.logic;

import com.mofang.chat.guild.global.ResultValue;
import com.mofang.framework.web.server.reactor.context.HttpRequestContext;

/**
 * 
 * @author zhaodx
 *
 */
public interface GuildGroupUserLogic
{
	public ResultValue join(HttpRequestContext context) throws Exception;
	
	public ResultValue quit(HttpRequestContext context) throws Exception;
	
	public ResultValue updateReceiveNotify(HttpRequestContext context) throws Exception;
	
	public ResultValue getReceiveNotify(HttpRequestContext context) throws Exception;
	
	public ResultValue delete(HttpRequestContext context) throws Exception;
	
	public ResultValue getList(HttpRequestContext context) throws Exception;
}