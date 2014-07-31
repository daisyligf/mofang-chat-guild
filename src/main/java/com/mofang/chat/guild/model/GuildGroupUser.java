package com.mofang.chat.guild.model;

import java.util.Date;

import com.mofang.framework.data.mysql.core.annotation.ColumnName;
import com.mofang.framework.data.mysql.core.annotation.TableName;

/**
 * 
 * @author zhaodx
 *
 */
@TableName(name="guild_group_user")
public class GuildGroupUser
{
	@ColumnName(name="group_id")
	private Long groupId;
	@ColumnName(name="user_id")
	private Long userId;
	@ColumnName(name="receive_notify")
	private Integer receiveNotify;
	@ColumnName(name="create_time")
	private Date createTime;
	
	public Long getGroupId()
	{
		return groupId;
	}
	
	public void setGroupId(Long groupId)
	{
		this.groupId = groupId;
	}
	
	public Long getUserId()
	{
		return userId;
	}
	
	public void setUserId(Long userId)
	{
		this.userId = userId;
	}
	
	public Integer getReceiveNotify()
	{
		return receiveNotify;
	}

	public void setReceiveNotify(Integer receiveNotify)
	{
		this.receiveNotify = receiveNotify;
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