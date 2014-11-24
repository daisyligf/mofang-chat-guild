package com.mofang.chat.guild.cron.task;

import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mofang.chat.guild.component.UserComponent;
import com.mofang.chat.guild.entity.User;
import com.mofang.chat.guild.global.GlobalObject;
import com.mofang.chat.guild.model.GuildUser;
import com.mofang.chat.guild.mysql.GuildDao;
import com.mofang.chat.guild.mysql.impl.GuildDaoImpl;
import com.mofang.chat.guild.redis.GuildUserRedis;
import com.mofang.chat.guild.redis.impl.GuildUserRedisImpl;

/**
 * 
 * @author daisyli
 *
 */
public class GuildUserListUpdateExecutor implements Runnable {

    private GuildDao guildDao = GuildDaoImpl.getInstance();
    private GuildUserRedis guildUserRedis = GuildUserRedisImpl.getInstance();
    private UserComponent userComponent = UserComponent.getInstance();
    
    @Override
    public void run() {
	try 
	{
	    long start = System.currentTimeMillis();
	    List<Long> guildIds = guildDao.getNormalGuildIds();
	    JSONArray data = null;
	    JSONObject userJson = null;
	    for (Long guildId : guildIds) {
		data = new JSONArray();
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
//		guildUserRedis.deleteGuildUserInfoList(guildId);
//		guildUserRedis.saveGuildUserInfoList(guildId, data.toString());
		
	    }
	    long end = System.currentTimeMillis();
	    GlobalObject.INFO_LOG.info("GuildUserListUpdateExecutor cost " + (end-start)/1000 +  " s");
	    GlobalObject.INFO_LOG.info("GuildUserListUpdateExecutor execute completed.");
	} 
	catch (Exception e) 
	{
	    GlobalObject.ERROR_LOG.error("GuildUserListUpdateExecutor.run throw an error.", e);
	}
	
    }
    
    private JSONObject getUserInfo(long guildId, long userId)
	{
		try
		{
			JSONObject json = new JSONObject();
			json.put("uid", userId);
			
			///获取用户基本信息
			User user = userComponent.getInfo(userId);
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
			GlobalObject.ERROR_LOG.error("at GuildUserListUpdateExecutor.getUserInfo throw an error. ", e);
			return null;
		}
	}
}
