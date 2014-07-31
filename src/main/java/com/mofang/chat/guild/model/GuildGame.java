package com.mofang.chat.guild.model;

import java.util.Date;

import com.mofang.framework.data.mysql.core.annotation.ColumnName;
import com.mofang.framework.data.mysql.core.annotation.TableName;

/**
 * 
 * @author zhaodx
 *
 */
@TableName(name="guild_game")
public class GuildGame
{
	@ColumnName(name="guild_id")
	private Long guildId;
	@ColumnName(name="game_id")
	private Integer gameId;
	@ColumnName(name="create_time")
	private Date createTime;
	
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
	
	public Date getCreateTime() 
	{
		return createTime;
	}
	
	public void setCreateTime(Date createTime)
	{
		this.createTime = createTime;
	}
}