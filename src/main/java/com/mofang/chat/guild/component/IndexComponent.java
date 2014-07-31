package com.mofang.chat.guild.component;

import org.apache.solr.common.SolrInputDocument;

import com.mofang.chat.guild.global.GlobalObject;

/**
 * 
 * @author zhaodx
 *
 */
public class IndexComponent
{
	public static void add(SolrInputDocument doc) throws Exception
	{
		try
		{
			GlobalObject.INDEX_SOLR_SERVER.add(doc);
			GlobalObject.INDEX_SOLR_SERVER.commit();
		}
		catch (Exception e)
		{
			throw e;
		}
	}
	
	public static void deleteById(String id)  throws Exception
	{
		try
		{
			GlobalObject.INDEX_SOLR_SERVER.deleteById(id);
			GlobalObject.INDEX_SOLR_SERVER.commit();
		} 
		catch (Exception e)
		{
			throw e;
		}
	}
}