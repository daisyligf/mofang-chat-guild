package com.mofang.chat.guild.model;

import java.util.Date;

import org.json.JSONObject;

import com.mofang.chat.guild.global.common.GuildGroupType;
import com.mofang.framework.data.mysql.core.annotation.ColumnName;
import com.mofang.framework.data.mysql.core.annotation.PrimaryKey;
import com.mofang.framework.data.mysql.core.annotation.TableName;

/**
 * 
 * @author zhaodx
 *
 */
@TableName(name="guild_group")
public class GuildGroup
{
	@PrimaryKey
	@ColumnName(name="group_id")
	private Long groupId;
	@ColumnName(name="guild_id")
	private Long guildId;
	@ColumnName(name="game_id")
	private Integer gameId;
	@ColumnName(name="type")
	private Integer type;
	@ColumnName(name="creator_id")
	private Long creatorId;
	@ColumnName(name="group_name")
	private String groupName;
	@ColumnName(name="avatar")
	private String avatar;
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
	
	public Long getGuildId()
	{
		return guildId;
	}
	
	public void setGuildId(Long guildId)
	{
		this.guildId = guildId;
	}
	
	public Integer getGameId()
	{
		return gameId;
	}
	
	public void setGameId(Integer gameId)
	{
		this.gameId = gameId;
	}
	
	public Integer getType()
	{
		return type;
	}
	
	public void setType(Integer type)
	{
		this.type = type;
	}
	
	public Long getCreatorId()
	{
		return creatorId;
	}
	
	public void setCreatorId(Long creatorId)
	{
		this.creatorId = creatorId;
	}
	
	public String getGroupName()
	{
		return groupName;
	}
	
	public void setGroupName(String groupName)
	{
		this.groupName = groupName;
	}
	
	public String getAvatar()
	{
		return avatar;
	}
	
	public void setAvatar(String avatar)
	{
		this.avatar = avatar;
	}
	
	public Date getCreateTime()
	{
		return createTime;
	}
	
	public void setCreateTime(Date createTime)
	{
		this.createTime = createTime;
	}
	
	public JSONObject toJson()
	{
		JSONObject json = new JSONObject();
		try
		{
			json.put("groupId", groupId);
			json.put("guildId", guildId);
			json.put("gameId", gameId);
			json.put("type", type);
			json.put("creatorId", creatorId);
			json.put("name", groupName == null ? "" : groupName);
			json.put("avatar", avatar == null ? "" : avatar);
			json.put("createTime", createTime == null ? System.currentTimeMillis() : createTime.getTime());
			return json;
		}
		catch(Exception e)
		{
			return null;
		}
	}
	
	public static GuildGroup buildByJson(JSONObject json)
	{
		GuildGroup model = new GuildGroup();
		try
		{
			model.setGroupId(json.optLong("groupId", 0L));
			model.setGuildId(json.optLong("guildId", 0L));
			model.setGameId(json.optInt("gameId", 0));
			model.setType(json.optInt("type", GuildGroupType.GUILD));
			model.setCreatorId(json.optLong("creatorId", 0L));
			model.setGroupName(json.optString("name", ""));
			model.setAvatar(json.optString("avatar", ""));
			long time = json.optLong("createTime", System.currentTimeMillis());
			model.setCreateTime(new Date(time));
			return model;
		}
		catch(Exception e)
		{
			return null;
		}
	}
}