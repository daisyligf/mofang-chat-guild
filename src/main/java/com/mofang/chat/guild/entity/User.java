package com.mofang.chat.guild.entity;

import org.json.JSONObject;

import com.mofang.chat.guild.global.common.UserStatus;
import com.mofang.chat.guild.global.common.UserType;

/**
 * 
 * @author zhaodx
 *
 */
public class User
{
	private Long userId;
	private String sessionId;
	private String nickName;
	private String avatar;
	private int type;
	private int status;
	private int gender;
	
	public Long getUserId()
	{
		return userId;
	}
	
	public void setUserId(Long userId) 
	{
		this.userId = userId;
	}

	public String getSessionId()
	{
		return sessionId;
	}

	public void setSessionId(String sessionId)
	{
		this.sessionId = sessionId;
	}
	
	public String getNickName()
	{
		return nickName;
	}
	
	public void setNickName(String nickName)
	{
		this.nickName = nickName;
	}
	
	public String getAvatar() 
	{
		return avatar;
	}
	
	public void setAvatar(String avatar) 
	{
		this.avatar = avatar;
	}

	public int getType() 
	{
		return type;
	}

	public void setType(int type) 
	{
		this.type = type;
	}

	public int getStatus()
	{
		return status;
	}

	public void setStatus(int status)
	{
		this.status = status;
	}

	public int getGender() 
	{
		return gender;
	}

	public void setGender(int gender)
	{
		this.gender = gender;
	}

	public JSONObject toJson()
	{
		JSONObject json = new JSONObject();
		try
		{
			json.put("id", userId);
			json.put("nick_name", nickName == null ? "" : nickName);
			json.put("avatar", avatar == null ? "" : avatar);
			json.put("type", type);
			json.put("status", status);
			json.put("gender", gender);
			return json;
		}
		catch(Exception e)
		{
			return null;
		}
	}
	
	public static User buildByJson(JSONObject json)
	{
		User model = new User();
		try
		{
			model.setUserId(json.optLong("id", 0L));
			model.setNickName(json.optString("nick_name", ""));
			model.setAvatar(json.optString("avatar", ""));
			model.setType(json.optInt("type", UserType.NORMAL));
			model.setStatus(json.optInt("status", UserStatus.NORMAL));
			model.setGender(json.optInt("gender", 1));
			return model;
		}
		catch(Exception e)
		{
			return null;
		}
	}
}