package com.mofang.chat.guild.mysql.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mofang.chat.guild.global.GlobalObject;
import com.mofang.chat.guild.model.GuildUser;
import com.mofang.chat.guild.mysql.GuildUserDao;
import com.mofang.framework.data.mysql.AbstractMysqlSupport;
import com.mofang.framework.data.mysql.core.criterion.operand.AndOperand;
import com.mofang.framework.data.mysql.core.criterion.operand.EqualOperand;
import com.mofang.framework.data.mysql.core.criterion.operand.NotEqualOperand;
import com.mofang.framework.data.mysql.core.criterion.operand.Operand;
import com.mofang.framework.data.mysql.core.criterion.operand.WhereOperand;
import com.mofang.framework.data.mysql.core.meta.ResultData;
import com.mofang.framework.data.mysql.core.meta.RowData;

/**
 * 
 * @author zhaodx
 *
 */
public class GuildUserDaoImpl extends AbstractMysqlSupport<GuildUser> implements GuildUserDao
{
	private final static GuildUserDaoImpl DAO = new GuildUserDaoImpl();
	
	private GuildUserDaoImpl()
	{
		try
		{
			super.setMysqlPool(GlobalObject.MYSQL_CONNECTION_POOL);
		}
		catch(Exception e)
		{}
	}
	
	public static GuildUserDaoImpl getInstance()
	{
		return DAO;
	}

	@Override
	public void add(GuildUser model) throws Exception
	{
		super.invokeInsert(model);
	}

	@Override
	public void delete(long guildId, long userId) throws Exception
	{
		Operand where = new WhereOperand();
		Operand equal = new EqualOperand("guild_id", guildId);
		Operand and = new AndOperand();
		Operand equal2 = new EqualOperand("user_id", userId);
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

	@Override
	public void updateRole(long guildId, long userId, int role) throws Exception
	{
		StringBuilder strSql = new StringBuilder();
		strSql.append("update guild_user ");
		strSql.append("set role=" + role + " ");
		strSql.append("where guild_id=" + guildId + " and user_id=" + userId);
		super.invokeExecute(strSql.toString());
	}

	@Override
	public void updateStatus(long guildId, long userId, int status) throws Exception
	{
		StringBuilder strSql = new StringBuilder();
		strSql.append("update guild_user ");
		strSql.append("set status=" + status + " ");
		strSql.append("where guild_id=" + guildId + " and user_id=" + userId);
		super.invokeExecute(strSql.toString());
	}

	@Override
	public void updateLastLoginTime(long guildId, long userId, Date datetime) throws Exception
	{		
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		StringBuilder strSql = new StringBuilder();
		strSql.append("update guild_user ");
		strSql.append("set last_login_time='" + format.format(datetime) + "' ");
		strSql.append("where guild_id=" + guildId + " and user_id=" + userId);
		super.invokeExecute(strSql.toString());
	}

	@Override
	public List<GuildUser> getList(long guildId) throws Exception
	{
		Operand where = new WhereOperand();
		Operand equal = new EqualOperand("guild_id", guildId);
		where.append(equal);
		return super.getList(where);
	}

	@Override
	public long getJoinCount(long userId) throws Exception
	{
		Operand where = new WhereOperand();
		Operand equal = new EqualOperand("user_id", userId);
		Operand equal2 = new EqualOperand("status", 1);
		Operand and = new AndOperand();
		Operand notEqual = new NotEqualOperand("role", 1);
		where.append(equal).append(and).append(equal2).append(and).append(notEqual);
		return super.getCount(where);
	}

	@Override
	public Map<Long, Integer> getUnloginMemberCount(String dateAgo) throws Exception
	{
		StringBuilder strSql = new StringBuilder();
		strSql.append("select guild_id, count(1) from guild_user ");
		strSql.append("where status = 1 and last_login_time <= '" + dateAgo + "' ");
		strSql.append("group by guild_id");
		ResultData data = super.executeQuery(strSql.toString());
		if(null == data || (!data.getExecuteResult()))
			return null;
		
		List<RowData> rows = data.getQueryResult();
		if(null == rows || rows.size() == 0)
			return null;
		
		long guildId = 0L;
		int count = 0;
		Map<Long, Integer> map = new HashMap<Long, Integer>();
		for(RowData row : rows)
		{
			guildId = row.getLong(0);
			count = row.getLong(1).intValue();
			map.put(guildId, count);
		}
		return map;
	}
}