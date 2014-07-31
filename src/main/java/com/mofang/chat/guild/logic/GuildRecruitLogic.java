package com.mofang.chat.guild.logic;

import com.mofang.chat.guild.global.ResultValue;
import com.mofang.framework.web.server.reactor.context.HttpRequestContext;

/**
 * 
 * @author zhaodx
 *
 */
public interface GuildRecruitLogic
{
	public ResultValue publish(HttpRequestContext context) throws Exception;
	
	public ResultValue audit(HttpRequestContext context) throws Exception;
	
	public ResultValue getRecruitList(HttpRequestContext context) throws Exception;
}