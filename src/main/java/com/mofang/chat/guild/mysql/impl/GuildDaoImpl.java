package com.mofang.chat.guild.mysql.impl;

import java.util.ArrayList;
import java.util.List;

import com.mofang.chat.guild.global.GlobalObject;
import com.mofang.chat.guild.model.Guild;
import com.mofang.chat.guild.mysql.GuildDao;
import com.mofang.framework.data.mysql.AbstractMysqlSupport;
import com.mofang.framework.data.mysql.core.criterion.operand.AndOperand;
import com.mofang.framework.data.mysql.core.criterion.operand.EqualOperand;
import com.mofang.framework.data.mysql.core.criterion.operand.Operand;
import com.mofang.framework.data.mysql.core.criterion.operand.WhereOperand;
import com.mofang.framework.data.mysql.core.meta.ResultData;
import com.mofang.framework.data.mysql.core.meta.RowData;

/**
 * 
 * @author zhaodx
 *
 */
public class GuildDaoImpl extends AbstractMysqlSupport<Guild> implements GuildDao
{
	private final static GuildDaoImpl DAO = new GuildDaoImpl();
	
	private GuildDaoImpl()
	{
		try
		{
			super.setMysqlPool(GlobalObject.MYSQL_CONNECTION_POOL);
		}
		catch(Exception e)
		{}
	}
	
	public static GuildDaoImpl getInstance()
	{
		return DAO;
	}

	@Override
	public void add(Guild model) throws Exception
	{
		super.invokeInsert(model);
	}

	@Override
	public void update(Guild model) throws Exception
	{
		super.invokeUpdateByPrimaryKey(model);
	}

	@Override
	public void delete(long guildId) throws Exception
	{	
		super.invokeDeleteByPrimaryKey(guildId);
	}

	@Override
	public void updateStatus(long guildId, int status) throws Exception
	{
		StringBuilder strSql = new StringBuilder();
		strSql.append("update guild set status=" + status + " where guild_id=" + guildId);
		super.invokeExecute(strSql.toString());
	}

	@Override
	public Guild getInfo(long guildId) throws Exception
	{
		return super.getByPrimaryKey(guildId);
	}

	@Override
	public long getCreatedCount(long userId) throws Exception 
	{
		Operand where = new WhereOperand();
		Operand equal = new EqualOperand("creator_id", userId);
		Operand and = new AndOperand();
		Operand equal2 = new EqualOperand("status", 1);
		where.append(equal).append(and).append(equal2);
		return super.getCount(where);
	}

	@Override
	public List<Long> getDismissGuildIds(String dateAgo, int minMemberCount) throws Exception
	{
		StringBuilder strSql = new StringBuilder();
		strSql.append("select a.guild_id from ");
		strSql.append("(select guild_id from guild ");
		strSql.append("where create_time < '" + dateAgo + "') as a ");
		strSql.append("inner join ");
		strSql.append("(select guild_id from guild_user ");
		strSql.append("where status = 1 ");
		strSql.append("group by guild_id ");
		strSql.append("having count(user_id) < " + minMemberCount + ") as b ");
		strSql.append("on a.guild_id = b.guild_id ");
		ResultData data = super.executeQuery(strSql.toString());
		if(null == data)
			return null;
		
		List<RowData> rows = data.getQueryResult();
		if(null == rows || rows.size() == 0)
			return null;
		
		List<Long> list = new ArrayList<Long>();
		for(RowData row : rows)
			list.add(row.getLong(0));
		return list;
	}
	
	public List<Long> getInactiveGuildIds(String date, int minMemberCount) throws Exception
	{
	    	StringBuilder strSql = new StringBuilder();
	    	strSql.append("select a.guild_id from (select guild_id from guild ");
	    	strSql.append("where date_format(create_time, '%Y-%m-%d') = '").append(date);
	    	strSql.append("') a inner join (select a.guild_id, count(a.user_id) from guild_user a ");
	    	strSql.append("group by guild_id having count(user_id) < ").append(minMemberCount);
	    	strSql.append(") b on a.guild_id = b.guild_id ");
	    	ResultData data = super.executeQuery(strSql.toString());
	    	if (null == data)
	    	    return null;
	    	List<RowData> rows = data.getQueryResult();
	    	if (null == rows || rows.size() == 0)
	    	    return null;

	    	List<Long> list = new ArrayList<Long>();
	    	for (RowData row : rows)
	    	    list.add(row.getLong(0));
	    	return list;
	}
	
	public void updateDismissTime(long guildId, String date) throws Exception
	{
	    	StringBuilder strSql = new StringBuilder();
	    	strSql.append("update guild set dismiss_time = '").append(date);
	    	strSql.append("'").append(" where guild_id = ").append(guildId);
	    	super.execute(strSql.toString());
	}
}