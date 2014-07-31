package com.mofang.chat.guild.mysql.impl;

import com.mofang.chat.guild.global.GlobalObject;
import com.mofang.chat.guild.model.GuildInform;
import com.mofang.chat.guild.mysql.GuildInformDao;
import com.mofang.framework.data.mysql.AbstractMysqlSupport;

/**
 * 
 * @author zhaodx
 *
 */
public class GuildInformDaoImpl extends AbstractMysqlSupport<GuildInform> implements GuildInformDao
{
	private final static GuildInformDaoImpl DAO = new GuildInformDaoImpl();
	
	private GuildInformDaoImpl()
	{
		try
		{
			super.setMysqlPool(GlobalObject.MYSQL_CONNECTION_POOL);
		}
		catch(Exception e)
		{}
	}
	
	public static GuildInformDaoImpl getInstance()
	{
		return DAO;
	}

	@Override
	public void add(GuildInform model) throws Exception
	{
		super.invokeInsert(model);
	}
}