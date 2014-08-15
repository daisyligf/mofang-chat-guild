package com.mofang.chat.guild.logic;

import com.mofang.chat.guild.global.ResultValue;
import com.mofang.framework.web.server.reactor.context.HttpRequestContext;

/**
 * 
 * @author zhaodx
 *
 */
public interface GuildUserLogic
{
	public ResultValue join(HttpRequestContext context) throws Exception;
	
	public ResultValue quit(HttpRequestContext context) throws Exception;
	
	public ResultValue audit(HttpRequestContext context) throws Exception;
	
	public ResultValue delete(HttpRequestContext context) throws Exception;
	
	public ResultValue changeRole(HttpRequestContext context) throws Exception;
	
	public ResultValue getList(HttpRequestContext context) throws Exception;
	
	public ResultValue userExists (HttpRequestContext context) throws Exception;
}