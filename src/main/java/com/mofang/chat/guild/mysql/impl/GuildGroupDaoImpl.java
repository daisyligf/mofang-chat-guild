package com.mofang.chat.guild.mysql.impl;

import com.mofang.chat.guild.global.GlobalObject;
import com.mofang.chat.guild.model.GuildGroup;
import com.mofang.chat.guild.mysql.GuildGroupDao;
import com.mofang.framework.data.mysql.AbstractMysqlSupport;
import com.mofang.framework.data.mysql.core.criterion.operand.EqualOperand;
import com.mofang.framework.data.mysql.core.criterion.operand.Operand;
import com.mofang.framework.data.mysql.core.criterion.operand.WhereOperand;

/**
 * 
 * @author zhaodx
 *
 */
public class GuildGroupDaoImpl extends AbstractMysqlSupport<GuildGroup> implements GuildGroupDao
{
	private final static GuildGroupDaoImpl DAO = new GuildGroupDaoImpl();
	
	private GuildGroupDaoImpl()
	{
		try
		{
			super.setMysqlPool(GlobalObject.MYSQL_CONNECTION_POOL);
		}
		catch(Exception e)
		{}
	}
	
	public static GuildGroupDaoImpl getInstance()
	{
		return DAO;
	}

	@Override
	public void add(GuildGroup model) throws Exception
	{
		super.invokeInsert(model);
	}

	@Override
	public void update(GuildGroup model) throws Exception
	{
		super.invokeUpdateByPrimaryKey(model);
	}

	@Override
	public void delete(long groupId) throws Exception
	{
		super.invokeDeleteByPrimaryKey(groupId);
	}

	@Override
	public void deleteByGuildId(long guildId) throws Exception
	{
		Operand where = new WhereOperand();
		Operand equal = new EqualOperand("guild_id", guildId);
		where.append(equal);
		super.invokeDeleteByWhere(where);
	}

	@Override
	public GuildGroup getInfo(long groupId) throws Exception
	{
		return super.getByPrimaryKey(groupId);
	}
}