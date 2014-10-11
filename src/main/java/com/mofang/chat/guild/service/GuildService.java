package com.mofang.chat.guild.service;

import java.util.List;

import org.apache.solr.common.SolrInputDocument;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mofang.chat.guild.model.Guild;

/**
 * 
 * @author zhaodx
 *
 */
public interface GuildService
{
	public void create(Guild model, JSONArray games) throws Exception;
	
	public void edit(Guild model) throws Exception;
	
	public void delete(Guild guild, int status, int event) throws Exception;
	
	public void upDown(Guild model, int optType, int listType) throws Exception;
	
	public JSONObject getInfo(long guildId) throws Exception;
	
	public JSONObject getGuildList(int type, int start, int end) throws Exception;
	
	public JSONArray getMyGuildList(long userId) throws Exception;
	
	public JSONObject getGuildListCount(long userId) throws Exception;
	
	public JSONObject getStatData(long guildId) throws Exception;
	
	public void updateDismissTime(long guildId) throws Exception;
	
	public SolrInputDocument convertToSolrDoc(Guild model, List<Integer> gameIds) throws Exception;
}