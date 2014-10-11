package com.mofang.chat.guild.component;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mofang.chat.guild.entity.User;
import com.mofang.chat.guild.global.GlobalObject;
import com.mofang.chat.guild.model.GuildUser;
import com.mofang.chat.guild.redis.GuildUserRedis;
import com.mofang.chat.guild.redis.impl.GuildUserRedisImpl;

/**
 * 
 * @author daisyli
 *
 */
public class UpdateGuildUserListComponent
{
    private final static int THREADS = Runtime.getRuntime().availableProcessors() + 1;
    private final ExecutorService executorService = Executors.newFixedThreadPool(THREADS);
    
    private static UpdateGuildUserListComponent instance = new UpdateGuildUserListComponent();
    
    public static UpdateGuildUserListComponent getInstance()
    {
	return instance;
    }
    
    public void updateAll(long guildId) 
    {
	try 
	{
	    GuildUserListUpdator updator = new GuildUserListUpdator(guildId);
	    Future<Boolean> result = executorService.submit(updator);
	    if (!result.get()) 
	    {
		GlobalObject.ERROR_LOG.error("GuildUserListUpdator failed.");
	    }
	} 
	catch (Exception e) 
	{
	    GlobalObject.ERROR_LOG
		    .error("UpdateGuildUserListComponent.updateAll throws an error."
			    + e);
	}
	
    }
    
    /**
     * 更新待审核用户列表+审核通过用户列表
     * @author daisyli
     *
     */
    public class GuildUserListUpdator implements Callable<Boolean>
    {
	private long guildId;
	private GuildUserRedis guildUserRedis = GuildUserRedisImpl.getInstance();

	public GuildUserListUpdator(long guildId)
	{
	    this.guildId = guildId;
	}
	
	@Override
	public Boolean call()
	{
	    try
	    {
		long start = System.currentTimeMillis();
		JSONArray data = new JSONArray();
		JSONObject userJson = null;
		Set<String> unauditedUserSet = guildUserRedis.getUnauditedUserList(guildId);
		for (String userId : unauditedUserSet) {
		    userJson = getUserInfo(guildId, Long.parseLong(userId));
			if(null != userJson)
				data.put(userJson);
		}
		Set<String> userSet = guildUserRedis.getUserList(guildId);
		for (String userId : userSet) {
		    userJson = getUserInfo(guildId, Long.parseLong(userId));
			if(null != userJson)
				data.put(userJson);
		}
		guildUserRedis.deleteGuildUserInfoList(guildId);
		guildUserRedis.saveGuildUserInfoList(guildId, data.toString());
		long end = System.currentTimeMillis();
		GlobalObject.INFO_LOG.info("GuildUserListUpdator.call cost " + (end-start) +  " ms");
	    } catch (Exception e)
	    {
		GlobalObject.ERROR_LOG.error("GuildUserListUpdator.call throw an error.", e);
	    }
	    return true;
	}
	
	private JSONObject getUserInfo(long guildId, long userId)
	{
		try
		{
			JSONObject json = new JSONObject();
			json.put("uid", userId);
			
			///获取用户基本信息
			User user = UserComponent.getInfo(userId);
			if(null != user)
			{
				json.put("nickname", user.getNickName());
				json.put("avatar", user.getAvatar());
				json.put("sex", user.getGender());
			}
			GuildUser guildUser = guildUserRedis.getInfo(guildId, userId);
			if(null != guildUser)
			{
				json.put("status", guildUser.getStatus());
				json.put("role", guildUser.getRole());
				json.put("last_login_time", guildUser.getLastLoginTime().getTime());
				json.put("postscript", guildUser.getPostscript());
			}
			return json;
		}
		catch(Exception e)
		{
			GlobalObject.ERROR_LOG.error("at GuildUserListUpdator.getUserInfo throw an error. ", e);
			return null;
		}
	}
	
	
    }
    
}
