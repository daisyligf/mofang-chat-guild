package com.mofang.chat.guild.service;

import org.json.JSONArray;

/**
 * 
 * @author zhaodx
 *
 */
public interface GuildGroupUserService
{
	public JSONArray getUserList(long groupId) throws Exception;
}