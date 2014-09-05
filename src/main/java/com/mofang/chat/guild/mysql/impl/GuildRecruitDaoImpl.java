package com.mofang.chat.guild.mysql.impl;

import java.util.List;

import com.mofang.chat.guild.global.GlobalObject;
import com.mofang.chat.guild.model.GuildRecruit;
import com.mofang.chat.guild.mysql.GuildRecruitDao;
import com.mofang.framework.data.mysql.AbstractMysqlSupport;
import com.mofang.framework.data.mysql.core.criterion.operand.AndOperand;
import com.mofang.framework.data.mysql.core.criterion.operand.EqualOperand;
import com.mofang.framework.data.mysql.core.criterion.operand.Operand;
import com.mofang.framework.data.mysql.core.criterion.operand.OrderByEntry;
import com.mofang.framework.data.mysql.core.criterion.operand.OrderByOperand;
import com.mofang.framework.data.mysql.core.criterion.operand.WhereOperand;
import com.mofang.framework.data.mysql.core.criterion.type.SortType;

/**
 * 
 * @author zhaodx
 *
 */
public class GuildRecruitDaoImpl extends AbstractMysqlSupport<GuildRecruit> implements GuildRecruitDao
{
	private final static GuildRecruitDaoImpl DAO = new GuildRecruitDaoImpl();
	
	private GuildRecruitDaoImpl()
	{
		try
		{
			super.setMysqlPool(GlobalObject.MYSQL_CONNECTION_POOL);
		}
		catch(Exception e)
		{}
	}
	
	public static GuildRecruitDaoImpl getInstance()
	{
		return DAO;
	}

	@Override
	public void add(GuildRecruit model) throws Exception
	{
		super.invokeInsert(model);
	}

	@Override
	public void updateStatus(int recruitId, int status) throws Exception
	{
		StringBuilder strSql = new StringBuilder();
		strSql.append("update guild_recruit ");
		strSql.append("set status=" + status + " ");
		strSql.append("where recruit_id=" + recruitId);
		super.invokeExecute(strSql.toString());
	}

	@Override
	public void updateStatusByGuild(long guildId, int status) throws Exception
	{
		StringBuilder strSql = new StringBuilder();
		strSql.append("update guild_recruit ");
		strSql.append("set status=" + status + " ");
		strSql.append("where guild_id=" + guildId);
		super.invokeExecute(strSql.toString());
	}

	@Override
	public void delete(int recruitId) throws Exception
	{
		super.invokeDeleteByPrimaryKey(recruitId);
	}

	@Override
	public void deleteByGuild(long guildId) throws Exception
	{
		Operand where = new WhereOperand();
		Operand equal = new EqualOperand("guild_id", guildId);
		where.append(equal);
		super.invokeDeleteByWhere(where);
	}

	@Override
	public GuildRecruit getInfo(int recruitId) throws Exception
	{
		return super.getByPrimaryKey(recruitId);
	}
	
	@Override
	public List<GuildRecruit> getList(long guildId, int status) throws Exception
	{	
	    	Operand where = new WhereOperand();
	    	Operand guildIdEqual = new EqualOperand("guild_id", guildId);
	    	Operand and = new AndOperand();
	    	Operand statusEqual = new EqualOperand("status", status);
	    	Operand order = new OrderByOperand(new OrderByEntry("audit_time", SortType.Desc));
	    	where.append(guildIdEqual);
	    	where.append(and);
	    	where.append(statusEqual);
	    	where.append(order);
		return super.getList(where);
	}
}