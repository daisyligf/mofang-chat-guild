package com.mofang.chat.guild.mysql.impl;

import java.util.List;

import com.mofang.chat.guild.global.GlobalObject;
import com.mofang.chat.guild.model.GuildGroupUser;
import com.mofang.chat.guild.mysql.GuildGroupUserDao;
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
public class GuildGroupUserDaoImpl extends AbstractMysqlSupport<GuildGroupUser> implements GuildGroupUserDao
{
	private final static GuildGroupUserDaoImpl DAO = new GuildGroupUserDaoImpl();
	
	private GuildGroupUserDaoImpl()
	{
		try
		{
			super.setMysqlPool(GlobalObject.MYSQL_CONNECTION_POOL);
		}
		catch(Exception e)
		{}
	}
	
	public static GuildGroupUserDaoImpl getInstance()
	{
		return DAO;
	}

	@Override
	public void add(GuildGroupUser model) throws Exception
	{
		super.invokeInsert(model);
	}

	@Override
	public void updateReceiveNotify(long groupId, long userId, int receiveNotify) throws Exception
	{
		StringBuilder strSql = new StringBuilder();
		strSql.append("update guild_group_user ");
		strSql.append("set receive_notify=" + receiveNotify + " ");
		strSql.append("where group_id=" + groupId + " and user_id=" + userId);
		super.invokeExecute(strSql.toString());
	}

	@Override
	public void delete(long groupId, long userId) throws Exception
	{
		Operand where = new WhereOperand();
		Operand equal = new EqualOperand("group_id", groupId);
		Operand and = new AndOperand();
		Operand equal2 = new EqualOperand("user_id", userId);
		where.append(equal).append(and).append(equal2);
		super.invokeDeleteByWhere(where);
	}

	@Override
	public void deleteByGroup(long groupId) throws Exception
	{
		Operand where = new WhereOperand();
		Operand equal = new EqualOperand("group_id", groupId);
		where.append(equal);
		super.invokeDeleteByWhere(where);
	}

	@Override
	public List<GuildGroupUser> getList(long groupId) throws Exception
	{
		Operand where = new WhereOperand();
		Operand equal = new EqualOperand("group_id", groupId);
		where.append(equal);
		return super.getList(where);
	}
}