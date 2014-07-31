package com.mofang.chat.guild.mysql.impl;

import com.mofang.chat.guild.global.GlobalObject;
import com.mofang.chat.guild.model.GuildGame;
import com.mofang.chat.guild.mysql.GuildGameDao;
import com.mofang.framework.data.mysql.AbstractMysqlSupport;
import com.mofang.framework.data.mysql.core.criterion.operand.AndOperand;
import com.mofang.framework.data.mysql.core.criterion.operand.EqualOperand;
import com.mofang.framework.data.mysql.core.criterion.operand.Operand;
import com.mofang.framework.data.mysql.core.criterion.operand.WhereOperand;

/**
 * 
 * @author zhaodx
 *
 */
public class GuildGameDaoImpl extends AbstractMysqlSupport<GuildGame> implements GuildGameDao
{
	private final static GuildGameDaoImpl DAO = new GuildGameDaoImpl();
	
	private GuildGameDaoImpl()
	{
		try
		{
			super.setMysqlPool(GlobalObject.MYSQL_CONNECTION_POOL);
		}
		catch(Exception e)
		{}
	}
	
	public static GuildGameDaoImpl getInstance()
	{
		return DAO;
	}
	
	@Override
	public void add(GuildGame model) throws Exception
	{
		super.invokeInsert(model);
	}

	@Override
	public void delete(long guildId, int gameId) throws Exception
	{
		Operand where = new WhereOperand();
		Operand equal = new EqualOperand("guild_id", guildId);
		Operand and = new AndOperand();
		Operand equal2 = new EqualOperand("game_id", gameId);
		where.append(equal).append(and).append(equal2);
		super.invokeDeleteByWhere(where);
	}

	@Override
	public void deleteByGuild(long guildId) throws Exception
	{
		Operand where = new WhereOperand();
		Operand equal = new EqualOperand("guild_id", guildId);
		where.append(equal);
		super.invokeDeleteByWhere(where);
	}
}