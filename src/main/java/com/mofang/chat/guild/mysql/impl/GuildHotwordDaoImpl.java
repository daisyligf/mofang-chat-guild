package com.mofang.chat.guild.mysql.impl;

import java.util.List;

import com.mofang.chat.guild.global.GlobalObject;
import com.mofang.chat.guild.model.GuildHotword;
import com.mofang.chat.guild.mysql.GuildHotwordDao;
import com.mofang.framework.data.mysql.AbstractMysqlSupport;
import com.mofang.framework.data.mysql.core.criterion.operand.EqualOperand;
import com.mofang.framework.data.mysql.core.criterion.operand.Operand;
import com.mofang.framework.data.mysql.core.criterion.operand.OrderByEntry;
import com.mofang.framework.data.mysql.core.criterion.operand.OrderByOperand;
import com.mofang.framework.data.mysql.core.criterion.operand.WhereOperand;
import com.mofang.framework.data.mysql.core.criterion.type.SortType;

/**
 * 
 * @author daisyli
 * 
 */
public class GuildHotwordDaoImpl extends AbstractMysqlSupport<GuildHotword>
	implements GuildHotwordDao 
{
    private final static GuildHotwordDaoImpl instance = new GuildHotwordDaoImpl();

    private GuildHotwordDaoImpl()
    {
	try 
	{
	    super.setMysqlPool(GlobalObject.MYSQL_CONNECTION_POOL);
	} 
	catch (Exception e) 
	{
	}
    }

    public static GuildHotwordDaoImpl getInstance() 
    {
	return instance;
    }
    
    public GuildHotword get(int hotwordId) throws Exception
    {
	return super.getByPrimaryKey(hotwordId);
    }
    
    public GuildHotword find(String word) throws Exception
    {
	Operand where = new WhereOperand();
	
	Operand equal = new EqualOperand("word", "'" + word + "'");
	where.append(equal);
	List<GuildHotword> list = super.getList(where);
	if (list != null && list.size() > 0) 
	{
	    return list.get(0);
	}
	   
	return null;
    }

    public boolean add(GuildHotword model) throws Exception 
    {
	return super.insert(model);
    }

    public boolean del(int hotwordId) throws Exception 
    {
	return super.deleteByPrimaryKey(hotwordId);
    }
    
    public List<GuildHotword> list() throws Exception
    {
	Operand where = new WhereOperand();
	Operand order = new OrderByOperand(new OrderByEntry("position", SortType.Desc));
	where.append(order);
		
	return super.getList(where);
    }
}
