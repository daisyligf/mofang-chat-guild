package com.mofang.chat.guild.model;

import java.util.Date;

import org.json.JSONObject;

import com.mofang.chat.guild.global.common.GuildRecruitStatus;
import com.mofang.framework.data.mysql.core.annotation.AutoIncrement;
import com.mofang.framework.data.mysql.core.annotation.ColumnName;
import com.mofang.framework.data.mysql.core.annotation.PrimaryKey;
import com.mofang.framework.data.mysql.core.annotation.TableName;

/**
 * 
 * @author zhaodx
 *
 */
@TableName(name="guild_recruit")
public class GuildRecruit
{
	@AutoIncrement
	@PrimaryKey
	@ColumnName(name="recurit_id")
	private Integer recruitId;
	@ColumnName(name="guild_id")
	private Long guildId;
	@ColumnName(name="content")
	private String content;
	@ColumnName(name="status")
	private Integer status;
	@ColumnName(name="apply_time")
	private Date applyTime;
	@ColumnName(name="audit_time")
	private Date auditTime;
	
	public Integer getRecruitId()
	{
		return recruitId;
	}
	
	public void setRecruitId(Integer recruitId) 
	{
		this.recruitId = recruitId;
	}
	
	public Long getGuildId()
	{
		return guildId;
	}
	
	public void setGuildId(Long guildId) 
	{
		this.guildId = guildId;
	}
	
	public String getContent() 
	{
		return content;
	}
	
	public void setContent(String content)
	{
		this.content = content;
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
	
	public JSONObject toJson()
	{
		JSONObject json = new JSONObject();
		try
		{
			json.put("recruitId", recruitId);
			json.put("guildId", guildId);
			json.put("content", content == null ? "" : content);
			json.put("status", status);
			long curtime = System.currentTimeMillis();
			json.put("applyTime", applyTime == null ? curtime : applyTime.getTime());
			json.put("auditTime", auditTime == null ? curtime : auditTime.getTime());
			return json;
		}
		catch(Exception e)
		{
			return null;
		}
	}
	
	public static GuildRecruit buildByJson(JSONObject json)
	{
		GuildRecruit model = new GuildRecruit();
		try
		{
			model.setRecruitId(json.optInt("recruitId", 0));
			model.setGuildId(json.optLong("guildId", 0L));
			model.setContent(json.optString("content", ""));
			model.setStatus(json.optInt("status", GuildRecruitStatus.NORMAL));
			long time = json.optLong("applyTime", System.currentTimeMillis());
			model.setApplyTime(new Date(time));
			time = json.optLong("auditTime", System.currentTimeMillis());
			model.setAuditTime(new Date(time));
			return model;
		}
		catch(Exception e)
		{
			return null;
		}
	}
}