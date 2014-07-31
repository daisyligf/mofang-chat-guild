package com.mofang.chat.guild.model;

import java.util.Date;

import org.json.JSONObject;

import com.mofang.chat.guild.global.common.GuildUserRole;
import com.mofang.chat.guild.global.common.GuildUserStatus;
import com.mofang.framework.data.mysql.core.annotation.ColumnName;
import com.mofang.framework.data.mysql.core.annotation.TableName;

/**
 * 
 * @author zhaodx
 *
 */
@TableName(name="guild_user")
public class GuildUser
{
	@ColumnName(name="guild_id")
	private Long guildId;
	@ColumnName(name="user_id")
	private Long userId;
	@ColumnName(name="postscript")
	private String postscript;
	@ColumnName(name="role")
	private Integer role;
	@ColumnName(name="status")
	private Integer status;
	@ColumnName(name="apply_time")
	private Date applyTime;
	@ColumnName(name="audit_time")
	private Date auditTime;
	@ColumnName(name="last_login_time")
	private Date lastLoginTime;
	
	public Long getGuildId() 
	{
		return guildId;
	}
	
	public void setGuildId(Long guildId)
	{
		this.guildId = guildId;
	}
	
	public Long getUserId()
	{
		return userId;
	}
	
	public void setUserId(Long userId) 
	{
		this.userId = userId;
	}
	
	public String getPostscript()
	{
		return postscript;
	}

	public void setPostscript(String postscript)
	{
		this.postscript = postscript;
	}

	public Integer getRole()
	{
		return role;
	}
	
	public void setRole(Integer role) 
	{
		this.role = role;
	}
	
	public Integer getStatus()
	{
		return status;
	}
	
	public void setStatus(Integer status)
	{
		this.status = status;
	}
	
	public Date getApplyTime() 
	{
		return applyTime;
	}
	
	public void setApplyTime(Date applyTime)
	{
		this.applyTime = applyTime;
	}
	
	public Date getAuditTime()
	{
		return auditTime;
	}
	
	public void setAuditTime(Date auditTime)
	{
		this.auditTime = auditTime;
	}
	
	public Date getLastLoginTime()
	{
		return lastLoginTime;
	}
	
	public void setLastLoginTime(Date lastLoginTime) 
	{
		this.lastLoginTime = lastLoginTime;
	}
	
	public JSONObject toJson()
	{
		JSONObject json = new JSONObject();
		try
		{
			json.put("guildid", guildId);
			json.put("userId", userId);
			json.put("postscript", postscript);
			json.put("role", role);
			json.put("status", status);
			long curtime = System.currentTimeMillis();
			json.put("applyTime", applyTime == null ? curtime : applyTime.getTime());
			json.put("auditTime", auditTime == null ? curtime : auditTime.getTime());
			json.put("lastLoginTime", lastLoginTime == null ? curtime : lastLoginTime.getTime());
			return json;
		}
		catch(Exception e)
		{
			return null;
		}
	}
	
	public static GuildUser buildByJson(JSONObject json)
	{
		GuildUser model = new GuildUser();
		try
		{
			model.setGuildId(json.optLong("id", 0L));
			model.setUserId(json.optLong("userId", 0L));
			model.setPostscript(json.optString("postscript", ""));
			model.setRole(json.optInt("role", GuildUserRole.MEMBER));
			model.setStatus(json.optInt("status", GuildUserStatus.UNAUDITED));
			long time = json.optLong("applyTime", System.currentTimeMillis());
			model.setApplyTime(new Date(time));
			time = json.optLong("auditTime", System.currentTimeMillis());
			model.setAuditTime(new Date(time));
			time = json.optLong("lastLoginTime", System.currentTimeMillis());
			model.setLastLoginTime(new Date(time));
			return model;
		}
		catch(Exception e)
		{
			return null;
		}
	}
}