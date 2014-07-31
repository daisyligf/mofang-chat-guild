package com.mofang.chat.guild.logic;

import com.mofang.chat.guild.global.ResultValue;
import com.mofang.framework.web.server.reactor.context.HttpRequestContext;

/**
 * 
 * @author zhaodx
 *
 */
public interface GuildLogic
{
	public ResultValue create(HttpRequestContext context) throws Exception;
	
	public ResultValue edit(HttpRequestContext context) throws Exception; 
	
	public ResultValue editByAdmin(HttpRequestContext context) throws Exception;
	
	public ResultValue dismiss(HttpRequestContext context) throws Exception;
	
	public ResultValue updown(HttpRequestContext context) throws Exception;
	
	public ResultValue delete(HttpRequestContext context) throws Exception;
	
	public ResultValue inform(HttpRequestContext context) throws Exception;
	
	public ResultValue getInfo(HttpRequestContext context) throws Exception;
	
	public ResultValue getList(HttpRequestContext context) throws Exception;
	
	public ResultValue getMyList(HttpRequestContext context) throws Exception;
	
	public ResultValue search(HttpRequestContext context) throws Exception;
	
	public ResultValue getStatData(HttpRequestContext context) throws Exception;
}