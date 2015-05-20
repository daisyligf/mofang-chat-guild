package com.mofang.chat.guild.service;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mofang.chat.guild.model.Guild;
import com.mofang.chat.guild.model.GuildUser;

/**
 * 
 * @author zhaodx
 *
 */
public interface GuildUserService
{
	public void join(GuildUser model, Guild guild) throws Exception;
	
	public void changeRole(long guildId, long userId, int role) throws Exception;
	
	public void delete(long guildId, long userId, int event) throws Exception;
	
	public void audit(Guild guild, long applyUid, int auditType) throws Exception;
	
	public JSONArray getAllUserList(long guildId) throws Exception;
	
	public JSONObject getUserInfo(long guildId, long userId);
}