package com.mofang.chat.guild.model;

import java.util.Date;

import com.mofang.framework.data.mysql.core.annotation.AutoIncrement;
import com.mofang.framework.data.mysql.core.annotation.ColumnName;
import com.mofang.framework.data.mysql.core.annotation.PrimaryKey;
import com.mofang.framework.data.mysql.core.annotation.TableName;

/**
 * 
 * @author zhaodx
 *
 */
@TableName(name="guild_inform")
public class GuildInform
{
	@PrimaryKey
	@AutoIncrement
	@ColumnName(name="id")
	private Integer id;
	@ColumnName(name="guild_id")
	private Long guildId;
	@ColumnName(name="inform_type")
	private Integer informType;
	@ColumnName(name="reason")
	private String reason;
	@ColumnName(name="user_id")
	private Long userId;
	@ColumnName(name="status")
	private Integer status;
	@ColumnName(name="create_time")
	private Date createTime;
	
	public Integer getId()
	{
		return id;
	}
	
	public void setId(Integer id)
	{
		this.id = id;
	}
	
	public Long getGuildId()
	{
		return guildId;
	}
	
	public void setGuildId(Long guildId)
	{
		this.guildId = guildId;
	}
	
	public Integer getInformType() 
	{
		return informType;
	}
	
	public void setInformType(Integer informType)
	{
		this.informType = informType;
	}
	
	public String getReason()
	{
		return reason;
	}
	
	public void setReason(String reason) 
	{
		this.reason = reason;
	}
	
	public Long getUserId() 
	{
		return userId;
	}

	public void setUserId(Long userId)
	{
		this.userId = userId;
	}

	public Integer getStatus() 
	{
		return status;
	}

	public void setStatus(Integer status)
	{
		this.status = status;
	}

	public Date getCreateTime()
	{
		return createTime;
	}
	
	public void setCreateTime(Date createTime)
	{
		this.createTime = createTime;
	}
}