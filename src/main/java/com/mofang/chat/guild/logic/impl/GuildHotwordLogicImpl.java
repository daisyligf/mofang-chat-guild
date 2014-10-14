package com.mofang.chat.guild.logic.impl;

import java.util.Date;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mofang.chat.guild.global.GlobalObject;
import com.mofang.chat.guild.global.ResultValue;
import com.mofang.chat.guild.global.ReturnCode;
import com.mofang.chat.guild.logic.GuildHotwordLogic;
import com.mofang.chat.guild.model.GuildHotword;
import com.mofang.chat.guild.mysql.GuildHotwordDao;
import com.mofang.chat.guild.mysql.impl.GuildHotwordDaoImpl;
import com.mofang.chat.guild.redis.GuildHotwordRedis;
import com.mofang.chat.guild.redis.impl.GuildHotwordRedisImpl;
import com.mofang.framework.util.StringUtil;
import com.mofang.framework.web.server.reactor.context.HttpRequestContext;

/**
 * 
 * @author daisyli
 *
 */
public class GuildHotwordLogicImpl implements GuildHotwordLogic
{
    private final static GuildHotwordLogicImpl instance = new GuildHotwordLogicImpl();
    
    private GuildHotwordDao guildHotwordDao = GuildHotwordDaoImpl.getInstance();
    private GuildHotwordRedis guildHotwordRedis = GuildHotwordRedisImpl.getInstance();
    
    private GuildHotwordLogicImpl() {}
    
    public static GuildHotwordLogicImpl getInstance()
    {
	return instance;
    }
    
    public ResultValue add(HttpRequestContext context) throws Exception
    {
	ResultValue result = new ResultValue();
	String postData = context.getPostData();
	if(StringUtil.isNullOrEmpty(postData))
	{
		result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
		return result;
	}
	
	try
	{
	    	JSONObject json = new JSONObject(postData);
		String word = json.optString("word", "");
		int position = json.optInt("position", 0);
		GuildHotword model = new GuildHotword();
		model.setWord(word);
		model.setPosition(position);
		model.setTime(new Date());
		if (guildHotwordDao.find(word) != null) 
		{
		    result.setCode(ReturnCode.GUILD_HOTWORD_EXISTS);
		    result.setMessage(GlobalObject.GLOBAL_MESSAGE.GUILD_HOTWORD_EXISTS);
		    return result;
		}
		// 添加到数据库
		guildHotwordDao.add(model);
		// 更新redis
		GuildHotword hotword = guildHotwordDao.find(word);
		guildHotwordRedis.add(hotword);
		
		result.setCode(ReturnCode.SUCCESS);
		result.setMessage(GlobalObject.GLOBAL_MESSAGE.SUCCESS);
		return result;
		
	}
	catch (Exception e)
	{
	    throw new Exception("GuildHotwordLogicImpl.add throws an error." + e);
	}
	
    }
    
    public ResultValue del(HttpRequestContext context) throws Exception
    {
	ResultValue result = new ResultValue();
	String postData = context.getPostData();
	if(StringUtil.isNullOrEmpty(postData))
	{
		result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
		return result;
	}
	try
	{
	    	JSONObject json = new JSONObject(postData);
		int wordId = json.optInt("word_id", 0);
		
		GuildHotword model = guildHotwordDao.get(wordId);
		
		// 更新Redis
		if (model != null)
		    guildHotwordRedis.del(model);
		// 删除数据库里的数据
		guildHotwordDao.del(wordId);
		
		result.setCode(ReturnCode.SUCCESS);
		result.setMessage(GlobalObject.GLOBAL_MESSAGE.SUCCESS);
		return result;
	}
	catch (Exception e)
	{
	    throw new Exception("GuildHotwordLogicImpl.del throws an error." + e);
	}
	
    }
    
    public ResultValue list(HttpRequestContext context) throws Exception
    {
	ResultValue result = new ResultValue();
	
	try
	{
		JSONArray data = new JSONArray();
		
		Set<String> set = guildHotwordRedis.list();
		for (String hotwordString : set)
		{
		    data.put(new JSONObject(hotwordString));
		}
		
		result.setData(data);
		result.setCode(ReturnCode.SUCCESS);
		result.setMessage(GlobalObject.GLOBAL_MESSAGE.SUCCESS);
		return result;
	}
	catch (Exception e)
	{
	    throw new Exception("GuildHotwordLogicImpl.list throws an error." + e);
	}
    }
}
